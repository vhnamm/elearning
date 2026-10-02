import React, {useEffect, useMemo, useState} from "react";
import { useOutletContext, useParams } from "react-router-dom";
import { Form, Input, Select, Button, Upload, message, Spin } from "antd";
import { getCourseBasicInfo } from "~services/course.service.js";
import {getCategoryTree, searchTopics} from "~services/category.service.js";
import useDebounce from "~hooks/useDebounce.js";

import {
    HolderOutlined,
    DeleteOutlined,
    PlusOutlined,
    BoldOutlined,
    ItalicOutlined,
    UnderlineOutlined,
    UnorderedListOutlined,
    OrderedListOutlined,
    LinkOutlined,
    PictureOutlined,
    CodeOutlined,
    SaveOutlined,
    AppstoreOutlined,
    UploadOutlined,
    CloseOutlined,
} from "@ant-design/icons";
import styles from "./CreateCourseOverview.module.scss";

const { TextArea } = Input;



const LEVEL_OPTIONS = [
    { value: "BEGINNER", label: "Người mới bắt đầu" },
    { value: "INTERMEDIATE", label: "Trung cấp" },
    { value: "ADVANCED", label: "Nâng cao" },
];

const TITLE_MAX = 100;
const SHORT_DESC_MAX = 250;
const MIN_OUTCOMES = 4;

const EDITOR_TOOLBAR_ICONS = [
    BoldOutlined,
    ItalicOutlined,
    UnderlineOutlined,
    "divider",
    UnorderedListOutlined,
    OrderedListOutlined,
    "divider",
    LinkOutlined,
    PictureOutlined,
    CodeOutlined,
];

export default function CreateCourseOverview() {
    const { setPageTitle } = useOutletContext() || {};
    const { courseId } = useParams();

    useEffect(() => {
        setPageTitle?.("Tổng quan khóa học");
        return () => setPageTitle?.("");
    }, [setPageTitle]);

    const [form] = Form.useForm();
    const titleValue = Form.useWatch("title", form) || "";
    const shortDescValue = Form.useWatch("shortDescription", form) || "";
    const selectedMainCategoryId = Form.useWatch("mainCategory", form)
    const selectedSubcategoryId = Form.useWatch("subCategory", form)

    const [thumbnailPreview, setThumbnailPreview] = useState(null);
    const [isThumbnailBlobUrl, setIsThumbnailBlobUrl] = useState(false);
    const [loading, setLoading] = useState(false);

    const [categoryTree, setCategoryTree] = useState([])
    const [topicOptions, setTopicOptions] = useState([])
    const [topicSearchLoading, setTopicSearchLoading] = useState(false)
    const [topicKeyword, setTopicKeyword] = useState("")
    const debouncedTopicKeyword = useDebounce(topicKeyword, 400)


    useEffect(() => {
        return () => {
            if (thumbnailPreview && isThumbnailBlobUrl) URL.revokeObjectURL(thumbnailPreview);
        };
    }, [thumbnailPreview, isThumbnailBlobUrl]);


    useEffect(() => {
        if (!courseId) return;

        const fetchFormData = async () => {
            setLoading(true);

            try {

                const [courseData, categoryTreeData] = await Promise.all([
                    getCourseBasicInfo(courseId),
                    getCategoryTree()
                ]);

                setCategoryTree(categoryTreeData)
                // Seed options bằng đúng các topic course đang chọn (có sẵn label),
                // các lựa chọn khác sẽ được nạp qua ô search khi user gõ.
                setTopicOptions(
                    (courseData?.topics ?? []).map(topic => ({value: topic.id, label: topic.name}))
                )

                const outcomes = courseData?.learningOutcomes?.length
                    ? courseData.learningOutcomes.map((item) => item.content)
                    : ["", "", "", ""];
                const prerequisites = courseData?.requiredSkills?.length
                    ? courseData.requiredSkills.map((item) => item.content)
                    : ["", ""];

                form.setFieldsValue({
                    title: courseData?.title,
                    shortDescription: courseData?.shortDescription,
                    longDescription: courseData?.description,
                    mainCategory: courseData?.categoryId,
                    subCategory: courseData?.subcategoryId,
                    outcomes,
                    prerequisites,
                    topics: (courseData?.topics ?? []).map((t) => t.id),
                    thumbnail: courseData?.thumbnailUrl ? [{ uid: "-1", name: "thumbnail", url: courseData.thumbnailUrl }] : [],
                    courseLevel: courseData?.level
                });

                if (courseData?.thumbnailUrl) {
                    setIsThumbnailBlobUrl(false);
                    setThumbnailPreview(courseData.thumbnailUrl);
                }

            } catch (error) {
                message.error("Không thể tải thông tin khóa học!");
            } finally {
                setLoading(false);
            }
        };

        fetchFormData();
    }, [courseId, form]);


    const mainCategoryOptions = useMemo(() => {
        return categoryTree.map(category => {
            return ({value: category.id, label: category.name})
        })
    }, [categoryTree])

    const subcategoryOptions = useMemo(() => {
        if(!selectedMainCategoryId || categoryTree.length === 0) return []

        //tim danh muc cha
        const currentCategory = categoryTree.find(cate => {
            return cate.id === selectedMainCategoryId
        })

        return currentCategory?.subcategories.map(sub => {
            return ({value: sub.id, label: sub.name})
        })
    }, [selectedMainCategoryId, categoryTree])


    // Topic là tag tự do, không phụ thuộc subcategory -> chỉ cần reset chính subcategory
    const handleMainCategoryChange = () => {
        form.setFieldValue("subCategory", undefined);
    };

    // Search-server cho Topic: gõ tới đâu set state tới đó, debounce lo phần gọi API
    useEffect(() => {
        let cancelled = false;

        const runSearch = async () => {
            setTopicSearchLoading(true);
            try {
                const results = await searchTopics(debouncedTopicKeyword);
                if (cancelled) return;

                const selectedIds = new Set(form.getFieldValue("topics") || []);
                setTopicOptions((prev) => {
                    const selectedOptions = prev.filter((opt) => selectedIds.has(opt.value));
                    const newOptions = results
                        .filter((t) => !selectedIds.has(t.id))
                        .map((t) => ({ value: t.id, label: t.name }));
                    return [...selectedOptions, ...newOptions];
                });
            } catch (error) {
                if (!cancelled) message.error("Không thể tìm kiếm chủ đề!");
            } finally {
                if (!cancelled) setTopicSearchLoading(false);
            }
        };

        runSearch();
        return () => { cancelled = true; };
    }, [debouncedTopicKeyword, form]);


    const MAX_THUMBNAIL_SIZE_MB = 5;

    const beforeUploadThumbnail = (file) => {
        const isImage = file.type.startsWith("image/");
        if (!isImage) {
            message.error("Vui lòng chọn một tệp hình ảnh!");
            return Upload.LIST_IGNORE;
        }
        const isUnderLimit = file.size / 1024 / 1024 < MAX_THUMBNAIL_SIZE_MB;
        if (!isUnderLimit) {
            message.error(`Dung lượng ảnh phải nhỏ hơn ${MAX_THUMBNAIL_SIZE_MB}MB!`);
            return Upload.LIST_IGNORE;
        }

        if (thumbnailPreview && isThumbnailBlobUrl) URL.revokeObjectURL(thumbnailPreview);
        setIsThumbnailBlobUrl(true);
        setThumbnailPreview(URL.createObjectURL(file));

        form.setFieldValue("thumbnail", [file]);

        form.validateFields(["thumbnail"]);

        return false; // Chặn Antd tự gửi request ngầm
    };

    const handleRemoveThumbnail = () => {
        if (thumbnailPreview && isThumbnailBlobUrl) URL.revokeObjectURL(thumbnailPreview);
        setIsThumbnailBlobUrl(false);
        setThumbnailPreview(null);

        form.setFieldValue("thumbnail", []);

        form.validateFields(["thumbnail"]);
    };


    const normalizeThumbnailFileList = (e) => {
        if (Array.isArray(e)) return e;
        return e?.fileList ?? [];
    };

    const onFinish = (values) => {
        console.log("Lưu nháp tổng quan khóa học:", values);
    };

    return (
        <div className={styles.container}>
            <div className={styles.wrapper}>
                <Spin spinning={loading} tip="Đang tải thông tin khóa học...">
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={onFinish}
                    initialValues={{
                        outcomes: ["", "", "", ""],
                        prerequisites: ["", ""],
                    }}
                >
                    {/* Header ghim cố định chứa Bước 1 và Nút Lưu nháp */}
                    <div className={styles.stickyHeader}>
                        <div className={styles.headerLeft}>
                            <span className={styles.stepBadge}>BƯỚC 1 TRÊN 4</span>
                            <h1 className={styles.pageTitle}>Bước 1: Tổng quan khóa học</h1>
                        </div>
                        <Button
                            type="primary"
                            htmlType="submit"
                            size="large"
                            icon={<SaveOutlined />}
                            className={styles.submitBtn}
                        >
                            Lưu nháp
                        </Button>
                    </div>

                    <p className={styles.pageSubtitle}>
                        Cung cấp các thông tin nền tảng, mô tả chi tiết và phân loại để học viên dễ dàng tìm
                        thấy khóa học của bạn trên nền tảng MótEdu.
                    </p>

                    <Form.Item
                        name="title"
                        colon={false}
                        className={styles.formItem}
                        label={
                            <div className={styles.labelRow}>
                                <span className={styles.fieldLabel}>
                                    Tên khóa học
                                </span>
                                <span className={styles.counter}>
                                       {titleValue.length} / {TITLE_MAX} ký tự
                                </span>
                            </div>
                        }
                        rules={[
                            { required: true, message: "Vui lòng nhập tên khóa học!" },
                            { max: TITLE_MAX, message: `Tên khóa học không được vượt quá ${TITLE_MAX} ký tự!` },
                        ]}

                    >
                        <Input
                            size="large"
                            maxLength={TITLE_MAX}
                            placeholder="Lập trình React 18 & TypeScript từ cơ bản đến nâng cao"
                        />
                    </Form.Item>

                    <Form.Item
                        name="shortDescription"
                        colon={false}
                        className={styles.formItem}
                        label={
                            <div className={styles.labelRow}>
                                <span className={styles.fieldLabel}>
                                    Mô tả ngắn
                                </span>
                                <span className={styles.counter}>
                                    {shortDescValue.length} / {SHORT_DESC_MAX} ký tự
                                </span>
                            </div>
                        }
                        rules={[
                            { required: true, message: "Vui lòng nhập mô tả ngắn!" },
                            {
                                max: SHORT_DESC_MAX,
                                message: `Mô tả ngắn không được vượt quá ${SHORT_DESC_MAX} ký tự!`,
                            },
                        ]}
                        extra={
                            <span className={styles.hint}>
                                Tóm tắt cô đọng 2-3 câu ngắn xuất hiện dưới tiêu đề khóa học khi hiển thị danh mục.
                            </span>
                        }
                    >
                        <TextArea
                            rows={3}
                            maxLength={SHORT_DESC_MAX}
                            placeholder="Nắm vững React 18 mới nhất, Hooks, Redux Toolkit và tối ưu hiệu năng ứng dụng web với TypeScript thông qua các dự án thực chiến chuẩn doanh nghiệp."
                        />
                    </Form.Item>

                    <Form.Item
                        name="thumbnail"
                        colon={false}
                        className={styles.formItem}
                        label={
                            <span className={styles.fieldLabel}>
                                Ảnh thumbnail khoá học
                            </span>
                        }
                        rules={[
                            {
                                validator(_, value) {
                                    // Kiểm tra xem mảng thumbnail có file nào không
                                    if (value && value.length > 0) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error("Vui lòng tải lên ảnh đại diện khóa học!"));
                                },
                            },
                        ]}
                        extra={
                            <span className={styles.hint}>
                                Khuyến nghị tỷ lệ 16:9 (ví dụ 1280x720px), định dạng JPG/PNG, dung lượng tối đa {MAX_THUMBNAIL_SIZE_MB}MB.
                            </span>
                        }
                    >
                        {/* Toàn bộ cấu trúc HTML / CSS bên trong GIỮ NGUYÊN HOÀN TOÀN */}
                        <div className={styles.thumbnailRow}>
                            <div className={styles.thumbnailBox}>
                                {thumbnailPreview ? (
                                    <>
                                        <img src={thumbnailPreview} alt="Xem trước ảnh đại diện khóa học" />
                                        <button
                                            type="button"
                                            className={styles.thumbnailRemoveBtn}
                                            onClick={handleRemoveThumbnail}
                                            aria-label="Xóa ảnh đại diện"
                                        >
                                            <CloseOutlined />
                                        </button>
                                    </>
                                ) : (
                                    <PictureOutlined className={styles.thumbnailPlaceholderIcon} />
                                )}
                            </div>

                            <div className={styles.thumbnailActions}>
                                <Upload
                                    accept="image/*"
                                    maxCount={1}
                                    showUploadList={false}
                                    beforeUpload={beforeUploadThumbnail}
                                >
                                    <Button icon={<UploadOutlined />}>
                                        {thumbnailPreview ? "Đổi ảnh khác" : "Tải ảnh lên"}
                                    </Button>
                                </Upload>
                                <p className={styles.thumbnailHint}>
                                    JPG, PNG — tối đa {MAX_THUMBNAIL_SIZE_MB}MB
                                </p>
                            </div>
                        </div>
                    </Form.Item>

                    <Form.Item
                        name="longDescription"
                        colon={false}
                        className={styles.formItem}
                        label={
                            <span className={styles.fieldLabel}>
                                Mô tả chi tiết khóa học (Long Description){" "}
                                <span className={styles.required}>*</span>
                            </span>
                        }
                        rules={[{ required: true, message: "Vui lòng nhập mô tả chi tiết!" }]}
                    >
                        <div className={styles.richEditor}>
                            <div className={styles.richToolbar}>
                                {EDITOR_TOOLBAR_ICONS.map((Icon, index) =>
                                    Icon === "divider" ? (
                                        <span key={`divider-${index}`} className={styles.toolbarDivider} />
                                    ) : (
                                        <button
                                            key={Icon.displayName || index}
                                            type="button"
                                            className={styles.toolbarBtn}
                                        >
                                            <Icon />
                                        </button>
                                    )
                                )}
                            </div>
                            <Form.Item name="longDescription" noStyle>
                                <TextArea
                                    variant="borderless"
                                    rows={6}
                                    className={styles.richTextarea}
                                    placeholder="Nhập mô tả toàn diện về mục tiêu, lộ trình học, phương pháp tiếp cận và lý do học viên nên tham gia khóa học này..."
                                />
                            </Form.Item>
                        </div>
                    </Form.Item>

                    <div className={styles.listSection}>
                        <div className={styles.listSectionHeader}>
                            <div>
                                <div className={styles.listSectionTitle}>
                                    Bạn sẽ học được gì (What you will learn){" "}
                                    <span className={styles.required}>*</span>
                                </div>
                                <div className={styles.listSectionDesc}>
                                    Liệt kê các kỹ năng cụ thể sau khi hoàn thành khóa học
                                </div>
                            </div>
                            <span className={styles.minBadge}>Tối thiểu {MIN_OUTCOMES} mục</span>
                        </div>

                        <Form.List name="outcomes">
                            {(fields, { add, remove }) => (
                                <>
                                    {fields.map((field) => (
                                        <div className={styles.dynamicRow} key={field.key}>
                                            <HolderOutlined className={styles.dragHandle} />
                                            <Form.Item {...field} noStyle>
                                                <Input
                                                    size="large"
                                                    placeholder="Nhập một kỹ năng học viên sẽ đạt được"
                                                />
                                            </Form.Item>
                                            <button
                                                type="button"
                                                className={styles.removeBtn}
                                                onClick={() => remove(field.name)}
                                                aria-label="Xóa mục"
                                            >
                                                <DeleteOutlined />
                                            </button>
                                        </div>
                                    ))}
                                    <button
                                        type="button"
                                        className={styles.addLink}
                                        onClick={() => add("")}
                                    >
                                        <PlusOutlined /> Thêm mục mới
                                    </button>
                                </>
                            )}
                        </Form.List>
                    </div>

                    <div className={styles.listSection}>
                        <div className={styles.listSectionHeader}>
                            <div>
                                <div className={styles.listSectionTitle}>
                                    Kỹ năng yêu cầu / Điều kiện tiên quyết (Prerequisites)
                                </div>
                                <div className={styles.listSectionDesc}>
                                    Những kiến thức hoặc công cụ học viên cần có trước khi bắt đầu
                                </div>
                            </div>
                        </div>

                        <Form.List name="prerequisites">
                            {(fields, { add, remove }) => (
                                <>
                                    {fields.map((field) => (
                                        <div className={styles.dynamicRow} key={field.key}>
                                            <HolderOutlined className={styles.dragHandle} />
                                            <Form.Item {...field} noStyle>
                                                <Input
                                                    size="large"
                                                    placeholder="Nhập một yêu cầu tiên quyết"
                                                />
                                            </Form.Item>
                                            <button
                                                type="button"
                                                className={styles.removeBtn}
                                                onClick={() => remove(field.name)}
                                                aria-label="Xóa mục"
                                            >
                                                <DeleteOutlined />
                                            </button>
                                        </div>
                                    ))}
                                    <button
                                        type="button"
                                        className={styles.addLink}
                                        onClick={() => add("")}
                                    >
                                        <PlusOutlined /> Thêm yêu cầu
                                    </button>
                                </>
                            )}
                        </Form.List>
                    </div>

                    <div className={styles.categoryRow}>
                        <Form.Item
                            name="mainCategory"
                            colon={false}
                            label={<span className={styles.fieldLabel}>Danh mục chính <span className={styles.required}>*</span></span>}
                            rules={[{ required: true, message: "Vui lòng chọn danh mục chính!" }]}
                        >
                            <Select
                                size="large"
                                options={mainCategoryOptions}
                                onChange={handleMainCategoryChange}
                                placeholder="Lập trình & CNTT"
                                suffixIcon={<AppstoreOutlined />}
                            />
                        </Form.Item>

                        <Form.Item
                            name="subCategory"
                            colon={false}
                            label={<span className={styles.fieldLabel}>Danh mục con <span className={styles.required}>*</span></span>}
                            rules={[{ required: true, message: "Vui lòng chọn danh mục con!" }]}
                        >
                            <Select
                                size="large"
                                options={subcategoryOptions}
                                placeholder="Lập trình Web"
                            />
                        </Form.Item>

                        <Form.Item
                            name="topics"
                            colon={false}
                            label={<span className={styles.fieldLabel}>Chủ đề cụ thể (Topic)</span>}
                        >
                            <Select
                                size="large"
                                mode="multiple"
                                options={topicOptions}
                                onSearch={setTopicKeyword}
                                filterOption={false}
                                loading={topicSearchLoading}
                                notFoundContent={topicSearchLoading ? "Đang tìm..." : "Gõ để tìm chủ đề"}
                                placeholder="Tìm và thêm chủ đề..."
                            />
                        </Form.Item>
                    </div>

                    <Form.Item
                        name="courseLevel"
                        colon={false}
                        className={styles.formItem}
                        label={<span className={styles.fieldLabel}>Trình độ khóa học (Course Level) <span className={styles.required}>*</span></span>}
                        rules={[{ required: true, message: "Vui lòng chọn trình độ khóa học!" }]}
                        extra={
                            <span className={styles.hint}>
                                Giúp gợi ý khóa học phù hợp với kinh nghiệm của người học.
                            </span>
                        }
                    >
                        <Select
                            size="large"
                            options={LEVEL_OPTIONS}
                            placeholder="Cơ bản / Người mới bắt đầu"
                        />
                    </Form.Item>
                </Form>
                </Spin>
            </div>
        </div>
    );
}