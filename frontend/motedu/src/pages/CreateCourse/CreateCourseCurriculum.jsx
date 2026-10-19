import { useEffect, useRef, useState } from "react";
import { useOutletContext, useParams } from "react-router-dom";
import { Button, Dropdown, Form, Input, Modal, Radio, Spin, Switch, Tooltip, message } from "antd";
import {getInstructorCurriculum, createNewSection, deleteSection as deleteSectionApi } from "~services/course.service.js";
import {
    CaretDownOutlined,
    CaretRightOutlined,
    CheckSquareOutlined,
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    FileAddOutlined,
    FileTextOutlined,
    HolderOutlined,
    MinusCircleOutlined,
    PlayCircleOutlined,
    PlusOutlined,
    SaveOutlined,
} from "@ant-design/icons";
import styles from "./CreateCourseCurriculum.module.scss";

const uid = () => Math.random().toString(36).slice(2, 10);

const formatDuration = (seconds) => {
    const total = Math.max(0, Math.round(seconds || 0));
    const m = String(Math.floor(total / 60)).padStart(2, "0");
    const s = String(total % 60).padStart(2, "0");
    return `${m}:${s}`;
};

/**
 * Đọc thời lượng + chụp 1 khung hình làm thumbnail từ file video cục bộ.
 * Blob URL chỉ dùng tạm để đọc metadata, được thu hồi ngay, KHÔNG dùng để phát video.
 */
const readVideoMeta = (file) =>
    new Promise((resolve) => {
        const url = URL.createObjectURL(file);
        const video = document.createElement("video");
        const done = (result) => {
            URL.revokeObjectURL(url);
            resolve(result);
        };
        video.preload = "metadata";
        video.muted = true;
        video.src = url;
        video.onerror = () => done({ duration: 0, thumbnail: null });
        video.onloadedmetadata = () => {
            const duration = video.duration;
            video.currentTime = Math.min(1, duration / 2);
            video.onseeked = () => {
                try {
                    const canvas = document.createElement("canvas");
                    canvas.width = 320;
                    canvas.height = 180;
                    canvas.getContext("2d").drawImage(video, 0, 0, 320, 180);
                    done({ duration, thumbnail: canvas.toDataURL("image/jpeg", 0.8) });
                } catch {
                    done({ duration, thumbnail: null });
                }
            };
        };
    });

// BE không trả hasContent, FE tự tính từ các trường của item.
// Item từ server có id số; item tạo mới ở FE có id chuỗi từ uid() cho tới khi được lưu.
// TODO(quiz-api): danh sách câu hỏi (item.questions) chưa tải được, BE mới trả questionCount.
const hasContent = (item) => {
    if (item.type === "QUIZ") return (item.questions?.length ?? item.questionCount ?? 0) > 0;
    if (item.contentType === "VIDEO") return item.videoStatus === "READY";
    if (item.contentType === "DOCUMENT") return !!item.fileName;
    return false; // Lecture vừa tạo, chưa chọn video hay bài đọc
};

const isDocument = (item) => item.type === "LECTURE" && item.contentType === "DOCUMENT";

const itemIcon = (item) => {
    if (item.type === "QUIZ") return <CheckSquareOutlined />;
    if (!hasContent(item)) return <FileAddOutlined />; // Lecture rỗng: chưa là video hay bài đọc
    return isDocument(item) ? <FileTextOutlined /> : <PlayCircleOutlined />;
};

const FILE_ACCEPT = {
    VIDEO: "video/*",
    DOCUMENT: ".pdf,.doc,.docx,.ppt,.pptx,.txt,.md",
};

const formatFileSize = (bytes = 0) =>
    bytes >= 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)} MB` : `${Math.max(1, Math.round(bytes / 1024))} KB`;

const itemSubtitle = (item) => {
    if (item.type === "QUIZ") {
        return `Quiz • ${item.questions?.length ?? item.questionCount ?? 0} câu hỏi`;
    }
    if (!hasContent(item)) return "Bài giảng • Chưa có nội dung";
    return isDocument(item) ? "Bài đọc" : `Video • ${formatDuration(item.videoDurationSeconds)}`;
};

const reorder = (list, from, to) => {
    if (from === to || from < 0 || to < 0) return list;
    const next = [...list];
    const [moved] = next.splice(from, 1);
    next.splice(to, 0, moved);
    return next;
};

export default function CreateCourseCurriculum() {
    const { setPageTitle } = useOutletContext() || {};
    const { courseId, versionId } = useParams();
    const [sections, setSections] = useState([]);
    const [loading, setLoading] = useState(false);
    const [expanded, setExpanded] = useState({}); // { [itemId]: boolean }
    const [editingSectionId, setEditingSectionId] = useState(null);
    const [addingSection, setAddingSection] = useState(false);
    const [newSectionTitle, setNewSectionTitle] = useState("");
    const [creatingSection, setCreatingSection] = useState(false);
    const [questionModal, setQuestionModal] = useState(null); // { sectionId, itemId, question|null }
    const [saving, setSaving] = useState(false);

    const dragRef = useRef(null); // { kind: "section" | "item", sectionId, id }
    const fileInputRef = useRef(null);
    const pendingUploadRef = useRef(null); // { sectionId, itemId }
    const [form] = Form.useForm();

    useEffect(() => {
        setPageTitle?.("Đề cương khóa học");
    }, [setPageTitle]);

    // Nạp đề cương của version khi vào trang.
    useEffect(() => {
        if (!courseId || !versionId) return;
        let cancelled = false;

        const fetchCurriculum = async () => {
            setLoading(true);
            try {
                const data = await getInstructorCurriculum(courseId, versionId);
                if (!cancelled) setSections(data?.sections ?? []);
            } catch {
                if (!cancelled) message.error("Không thể tải đề cương khóa học!");
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        fetchCurriculum();
        return () => {
            cancelled = true;
        };
    }, [courseId, versionId]);

    /* ---------- mutations ---------- */
    const updateSection = (sectionId, updater) =>
        setSections((prev) => prev.map((s) => (s.id === sectionId ? updater(s) : s)));

    const updateItem = (sectionId, itemId, patch) =>
        updateSection(sectionId, (s) => ({
            ...s,
            curriculumItems: s.curriculumItems.map((i) => (i.id === itemId ? { ...i, ...patch } : i)),
        }));

    const openAddSection = () => {
        setNewSectionTitle("");
        setAddingSection(true);
    };

    const cancelAddSection = () => {
        setAddingSection(false);
        setNewSectionTitle("");
    };



    const onCreateSection = async () => {
        const title = newSectionTitle.trim();
        if(!title){
            message.warning("Vui lòng nhập tiêu đề cho chương này")
            return;
        }
        setCreatingSection(true);
        try {
            console.log(courseId)
            const sectionResult  = await createNewSection(courseId, versionId, {title: title})
            console.log(sectionResult)
            setSections((prev) => [...prev, {...sectionResult.data, curriculumItems: sectionResult.curriculumItems ?? [] }])
            cancelAddSection()
        }catch  {
            message.error("Không thể tạo phần mới")
        }finally {
            setCreatingSection(false)
        }
    }

    const deleteSection = (section) =>
        Modal.confirm({
            title: "Xóa phần này?",
            content: `Phần "${section.title}" và toàn bộ bài học bên trong sẽ bị xóa.`,
            okText: "Xóa",
            cancelText: "Hủy",
            okButtonProps: { danger: true },
            onOk: () => handleConfirmDeleteSection(section.id),
        });

    const handleConfirmDeleteSection = async (sectionId) => {
        try {
            setSections((prev) => prev.filter(s => s.id != sectionId))
            const data = await deleteSectionApi(courseId, versionId, sectionId);

            message.success(data?.message || "Xóa thành công")
        }catch {
            message.error("Không thể xóa section, có lỗi xảy ra")
        }


    }
    const addItem = (sectionId, type) => {
        const item = {
            id: uid(),
            type,
            title: type === "QUIZ" ? "Bài trắc nghiệm mới" : "Bài giảng mới",
        };
        updateSection(sectionId, (s) => ({ ...s, curriculumItems: [...s.curriculumItems, item] }));
    };

    const deleteItem = (sectionId, itemId) =>
        updateSection(sectionId, (s) => ({ ...s, curriculumItems: s.curriculumItems.filter((i) => i.id !== itemId) }));

    const toggleExpand = (itemId) => setExpanded((prev) => ({ ...prev, [itemId]: !prev[itemId] }));

    /* ---------- video ---------- */
    const pickFile = (sectionId, itemId, kind) => {
        pendingUploadRef.current = { sectionId, itemId, kind };
        if (fileInputRef.current) {
            fileInputRef.current.accept = FILE_ACCEPT[kind];
            fileInputRef.current.click();
        }
    };

    const handleVideoChosen = async (e) => {
        const file = e.target.files?.[0];
        e.target.value = "";
        const target = pendingUploadRef.current;
        if (!file || !target) return;
        if (target.kind === "DOCUMENT") {
            updateItem(target.sectionId, target.itemId, {
                contentType: "DOCUMENT",
                fileName: file.name,
                fileSizeBytes: file.size,
                fileUrl: URL.createObjectURL(file),
            });
            setExpanded((prev) => ({ ...prev, [target.itemId]: true }));
            return;
        }
        // TODO(video-upload MVP): thay đoạn local-state này bằng luồng upload thật lên R2:
        //  1. POST /lectures/{id}/video/presigned-url  -> { uploadUrl }  (BE lưu videoRawKey, videoStatus=UPLOADING)
        //  2. PUT file lên uploadUrl (header Content-Type: video/mp4), hiển thị tiến độ upload
        //  3. POST /lectures/{id}/video/complete { durationSeconds } -> BE HeadObject kiểm tra file,
        //     set videoKey = videoRawKey, videoStatus = READY
        //  Lecture phải được lưu xuống BE (có id thật) trước bước 1.


        const meta = await readVideoMeta(file);
        updateItem(target.sectionId, target.itemId, {
            contentType: "VIDEO",
            videoStatus: "READY", // tạm, tới khi có luồng upload thật
            fileName: file.name,
            videoDurationSeconds: meta.duration,
            thumbnailUrl: meta.thumbnail,
        });
        setExpanded((prev) => ({ ...prev, [target.itemId]: true }));
    };

    const previewDocument = (item) => {
        if (!item.fileUrl) {
            message.info("Chưa có bản xem trước cho tệp này.");
            return;
        }
        window.open(item.fileUrl, "_blank", "opener");
    };

    // TODO(video-preview MVP): khi giảng viên bấm xem trước:
    //  1. GET /lectures/{id}/video/play-url -> { url } (presigned GET, TTL ~2h, BE kiểm tra quyền + videoStatus=READY)
    //  2. Mở Modal với <video src={url} controls autoPlay /> (trình duyệt tự gửi Range request để tua)
    //  Không phát từ blob cục bộ: preview phải đi qua đúng đường mà học viên sẽ xem.
    const previewVideo = () => {
        message.info("Xem trước video sẽ có sau khi tích hợp upload lên R2.");
    };

    /* ---------- quiz questions ---------- */
    // TODO(quiz-api): đồng bộ với DB (question_options: content, is_correct, position), FE chưa sửa:
    //  - Lưu (submitQuestion): đổi `correct` (chỉ số) thành cờ trên từng đáp án:
    //      options: values.options.map((content, i) => ({ content: content.trim(), isCorrect: i === values.correct }))
    //    KHÔNG gửi position, BE tự gán theo thứ tự mảng.
    //  - Mở sửa (openQuestionModal): đổi ngược lại:
    //      options: question.options.map((o) => o.content), correct: question.options.findIndex((o) => o.isCorrect)
    //  - Mỗi câu tối đa 4 đáp án (A-D): đổi giới hạn `fields.length < 6` thành `< 4` ở Form.List bên dưới.
    //  - hasContent của quiz tự tính ở FE từ questionCount > 0 (BE không trả hasContent).
    const openQuestionModal = (sectionId, itemId, question = null) => {
        form.setFieldsValue(
            question
                ? { text: question.text, options: question.options, correct: question.correct }
                : { text: "", options: ["", ""], correct: 0 },
        );
        setQuestionModal({ sectionId, itemId, question });
    };

    const submitQuestion = async () => {
        const values = await form.validateFields();
        const { sectionId, itemId, question } = questionModal;
        const options = values.options.map((o) => o.trim());
        const payload = { id: question?.id ?? uid(), text: values.text.trim(), options, correct: values.correct };

        updateSection(sectionId, (s) => ({
            ...s,
            curriculumItems: s.curriculumItems.map((i) => {
                if (i.id !== itemId) return i;
                const list = i.questions || [];
                const questions = question ? list.map((q) => (q.id === question.id ? payload : q)) : [...list, payload];
                return { ...i, questions };
            }),
        }));
        setExpanded((prev) => ({ ...prev, [itemId]: true }));
        setQuestionModal(null);
    };

    /* ---------- drag & drop (HTML5 native) ---------- */
    const dropSection = (targetId) => {
        const drag = dragRef.current;
        dragRef.current = null;
        if (drag?.kind !== "section") return;
        setSections((prev) =>
            reorder(
                prev,
                prev.findIndex((s) => s.id === drag.id),
                prev.findIndex((s) => s.id === targetId),
            ),
        );
    };

    const dropItem = (sectionId, targetId) => {
        const drag = dragRef.current;
        dragRef.current = null;
        // Chỉ sắp xếp lại trong cùng một Section
        if (drag?.kind !== "item" || drag.sectionId !== sectionId) return;
        updateSection(sectionId, (s) => ({
            ...s,
            curriculumItems: reorder(
                s.curriculumItems,
                s.curriculumItems.findIndex((i) => i.id === drag.id),
                s.curriculumItems.findIndex((i) => i.id === targetId),
            ),
        }));
    };

    const startDrag = (e, payload) => {
        e.stopPropagation();
        dragRef.current = payload;
        e.dataTransfer.effectAllowed = "move";
    };

    /* ---------- save ---------- */
    const handleSaveDraft = async () => {
        setSaving(true);
        try {
            // TODO: gọi API lưu đề cương khi backend sẵn sàng
            await new Promise((resolve) => setTimeout(resolve, 400));
            message.success("Đã lưu nháp đề cương khóa học");
        } finally {
            setSaving(false);
        }
    };

    /* ---------- render helpers ---------- */
    const renderItemBody = (section, item) => {
        if (item.type === "QUIZ") {
            const questions = item.questions || [];
            return (
                <div className={styles.quizBody}>
                    {questions.map((q, idx) => (
                        <div key={q.id} className={styles.questionRow}>
                            <span className={styles.questionText}>
                                Câu {idx + 1}. {q.text}
                            </span>
                            <Button
                                size="small"
                                icon={<EditOutlined />}
                                className={styles.questionEdit}
                                onClick={() => openQuestionModal(section.id, item.id, q)}
                            >
                                Chỉnh sửa
                            </Button>
                        </div>
                    ))}
                    <Button
                        type="dashed"
                        icon={<PlusOutlined />}
                        className={styles.addQuestionBtn}
                        onClick={() => openQuestionModal(section.id, item.id)}
                    >
                        Thêm câu hỏi mới
                    </Button>
                </div>
            );
        }

        if (isDocument(item)) {
            return (
                <div className={styles.videoBody}>
                    <div className={styles.docIcon}>
                        <FileTextOutlined />
                    </div>
                    <div className={styles.videoInfo}>
                        <div className={styles.fileName}>{item.fileName}</div>
                        <div className={styles.fileMeta}>{formatFileSize(item.fileSizeBytes)}</div>
                        <Button size="small" onClick={() => pickFile(section.id, item.id, "DOCUMENT")}>
                            Thay đổi tệp
                        </Button>
                    </div>
                    <Button icon={<EyeOutlined />} onClick={() => previewDocument(item)}>
                        Xem trước
                    </Button>
                </div>
            );
        }

        return (
            <div className={styles.videoBody}>
                <div className={styles.thumb}>
                    {item.thumbnailUrl ? (
                        <img src={item.thumbnailUrl} alt={item.title} />
                    ) : (
                        <PlayCircleOutlined className={styles.thumbPlaceholder} />
                    )}
                </div>
                <div className={styles.videoInfo}>
                    <div className={styles.fileName}>{item.fileName}</div>
                    <div className={styles.fileMeta}>Thời lượng {formatDuration(item.videoDurationSeconds)}</div>
                    <Button size="small" onClick={() => pickFile(section.id, item.id, "VIDEO")}>
                        Thay đổi video
                    </Button>
                </div>
                <Button icon={<EyeOutlined />} onClick={() => previewVideo(item)}>
                    Xem trước
                </Button>
            </div>
        );
    };

    const renderItem = (section, item) => {
        const isOpen = !!expanded[item.id];
        return (
            <div
                key={item.id}
                className={styles.item}
                onDragOver={(e) => e.preventDefault()}
                onDrop={(e) => {
                    e.stopPropagation();
                    dropItem(section.id, item.id);
                }}
            >
                <div className={styles.itemHeader}>
                    <div className={styles.itemLeft}>
                        <span
                            className={styles.dragHandle}
                            draggable
                            onDragStart={(e) => startDrag(e, { kind: "item", sectionId: section.id, id: item.id })}
                            aria-label="Kéo để sắp xếp"
                        >
                            <HolderOutlined />
                        </span>
                        <span className={styles.itemIcon}>{itemIcon(item)}</span>
                        <div className={styles.itemText}>
                            <Input
                                variant="borderless"
                                value={item.title}
                                className={styles.itemTitleInput}
                                onChange={(e) => updateItem(section.id, item.id, { title: e.target.value })}
                            />
                            <span className={styles.itemSub}>{itemSubtitle(item)}</span>
                        </div>
                    </div>
                    <div className={styles.itemRight}>
                        {item.type === "LECTURE" && (
                            <Tooltip title="Cho phép học viên chưa mua xem thử bài này">
                                <span className={styles.previewToggle}>
                                    <Switch
                                        size="small"
                                        checked={!!item.preview}
                                        onChange={(checked) => updateItem(section.id, item.id, { preview: checked })}
                                    />
                                    <span>Xem trước</span>
                                </span>
                            </Tooltip>
                        )}
                        {!hasContent(item) &&
                            (item.type === "QUIZ" ? (
                                <Button
                                    type="primary"
                                    size="small"
                                    icon={<PlusOutlined />}
                                    className={styles.addContentBtn}
                                    onClick={() => openQuestionModal(section.id, item.id)}
                                >
                                    Nội dung
                                </Button>
                            ) : (
                                <Dropdown
                                    trigger={["click"]}
                                    menu={{
                                        items: [
                                            { key: "VIDEO", icon: <PlayCircleOutlined />, label: "Video" },
                                            { key: "DOCUMENT", icon: <FileTextOutlined />, label: "Bài đọc (tệp tài liệu)" },
                                        ],
                                        onClick: ({ key }) => pickFile(section.id, item.id, key),
                                    }}
                                >
                                    <Button
                                        type="primary"
                                        size="small"
                                        icon={<PlusOutlined />}
                                        className={styles.addContentBtn}
                                    >
                                        Nội dung
                                    </Button>
                                </Dropdown>
                            ))}
                        <Button
                            type="text"
                            size="small"
                            icon={<DeleteOutlined />}
                            className={styles.iconBtn}
                            onClick={() => deleteItem(section.id, item.id)}
                            aria-label="Xóa bài học"
                        />
                        {hasContent(item) && (
                            <Button
                                type="text"
                                size="small"
                                className={styles.iconBtn}
                                icon={isOpen ? <CaretDownOutlined /> : <CaretRightOutlined />}
                                onClick={() => toggleExpand(item.id)}
                                aria-label={isOpen ? "Thu gọn" : "Mở rộng"}
                            />
                        )}
                    </div>
                </div>
                {hasContent(item) && isOpen && <div className={styles.itemExpanded}>{renderItemBody(section, item)}</div>}
            </div>
        );
    };

    const addMenu = (sectionId) => ({
        items: [
            { key: "LECTURE", icon: <PlayCircleOutlined />, label: "Bài giảng (Lecture)" },
            { key: "QUIZ", icon: <FileTextOutlined />, label: "Bài trắc nghiệm (Quiz)" },
        ],
        onClick: ({ key }) => addItem(sectionId, key),
    });

    return (
        <div className={styles.container}>
            <div className={styles.wrapper}>
                <div className={styles.stickyHeader}>
                    <div className={styles.headerLeft}>
                        <span className={styles.stepBadge}>BƯỚC 2 TRÊN 4</span>
                        <h1 className={styles.pageTitle}>Bước 2: Đề cương khóa học</h1>
                    </div>
                    <Button
                        type="primary"
                        size="large"
                        loading={saving}
                        icon={<SaveOutlined />}
                        className={styles.submitBtn}
                        onClick={handleSaveDraft}
                    >
                        Lưu nháp
                    </Button>
                </div>

                <p className={styles.pageSubtitle}>
                    Thêm các phần (Sections) và bài giảng (Items) để tạo thành nội dung khóa học của bạn.
                </p>

                <Spin spinning={loading} tip="Đang tải đề cương khóa học...">
                <div className={styles.sectionList}>
                    {sections.map((section, sIdx) => (
                        <div
                            key={section.id}
                            className={styles.section}
                            onDragOver={(e) => e.preventDefault()}
                            onDrop={() => dropSection(section.id)}
                        >
                            <div className={styles.sectionHeader}>
                                <span
                                    className={styles.dragHandle}
                                    draggable
                                    onDragStart={(e) => startDrag(e, { kind: "section", id: section.id })}
                                    aria-label="Kéo để sắp xếp phần"
                                >
                                    <HolderOutlined />
                                </span>
                                {editingSectionId === section.id ? (
                                    <Input
                                        autoFocus
                                        defaultValue={section.title}
                                        className={styles.sectionTitleInput}
                                        onBlur={(e) => {
                                            const title = e.target.value.trim();
                                            if (title) updateSection(section.id, (s) => ({ ...s, title }));
                                            setEditingSectionId(null);
                                        }}
                                        onPressEnter={(e) => e.currentTarget.blur()}
                                    />
                                ) : (
                                    <span className={styles.sectionTitle}>
                                        Phần {sIdx + 1}: {section.title}
                                    </span>
                                )}
                                <div className={styles.sectionActions}>
                                    <Button
                                        type="text"
                                        icon={<EditOutlined />}
                                        className={styles.iconBtn}
                                        onClick={() => setEditingSectionId(section.id)}
                                        aria-label="Sửa tiêu đề phần"
                                    />
                                    <Button
                                        type="text"
                                        icon={<DeleteOutlined />}
                                        className={styles.iconBtn}
                                        onClick={() => deleteSection(section)}
                                        aria-label="Xóa phần"
                                    />
                                </div>
                            </div>

                            <div className={styles.itemList}>
                                {section.curriculumItems.length === 0 && (
                                    <div className={styles.emptySection}>Phần này chưa có bài học nào.</div>
                                )}
                                {section.curriculumItems.map((item) => renderItem(section, item))}
                            </div>

                            <div className={styles.sectionFooter}>
                                <Dropdown menu={addMenu(section.id)} trigger={["click"]}>
                                    <Button icon={<PlusOutlined />}>Thêm bài giảng / bài tập</Button>
                                </Dropdown>
                            </div>
                        </div>
                    ))}
                </div>

                {addingSection ? (
                    <div className={styles.addSectionForm}>
                        <Input
                            autoFocus
                            maxLength={255}
                            value={newSectionTitle}
                            placeholder="Nhập tên phần, ví dụ: Giới thiệu khóa học"
                            onChange={(e) => setNewSectionTitle(e.target.value)}
                            onKeyDown={(e) => e.key === "Escape" && cancelAddSection()}
                        />
                        <Button onClick={cancelAddSection} disabled={creatingSection}>
                            Hủy
                        </Button>
                        <Button type="primary" loading={creatingSection} onClick={onCreateSection}>
                            Thêm phần
                        </Button>
                    </div>
                ) : (
                    <Button type="dashed" block icon={<PlusOutlined />} className={styles.addSectionBtn} onClick={openAddSection}>
                        Thêm Phần mới
                    </Button>
                )}
                </Spin>
            </div>

            <input ref={fileInputRef} type="file" hidden onChange={handleVideoChosen} />

            <Modal
                open={!!questionModal}
                title={questionModal?.question ? "Chỉnh sửa câu hỏi" : "Thêm câu hỏi mới"}
                okText="Lưu"
                cancelText="Hủy"
                onOk={submitQuestion}
                onCancel={() => setQuestionModal(null)}
                destroyOnHidden
                forceRender
            >
                <Form form={form} layout="vertical" requiredMark={false}>
                    <Form.Item
                        name="text"
                        label="Câu hỏi"
                        rules={[{ required: true, whitespace: true, message: "Vui lòng nhập câu hỏi" }]}
                    >
                        <Input.TextArea rows={2} placeholder="Nhập nội dung câu hỏi" />
                    </Form.Item>

                    <Form.Item name="correct" label="Đáp án (chọn đáp án đúng)">
                        <Radio.Group style={{ width: "100%" }}>
                            <Form.List name="options">
                                {(fields, { add, remove }) => (
                                    <>
                                        {fields.map((field, idx) => (
                                            <div key={field.key} className={styles.optionRow}>
                                                <Radio value={idx} />
                                                <Form.Item
                                                    name={field.name}
                                                    noStyle
                                                    rules={[{ required: true, whitespace: true, message: "Nhập đáp án" }]}
                                                >
                                                    <Input placeholder={`Đáp án ${idx + 1}`} />
                                                </Form.Item>
                                                {fields.length > 2 && (
                                                    <Button
                                                        type="text"
                                                        icon={<MinusCircleOutlined />}
                                                        onClick={() => {
                                                            const correct = form.getFieldValue("correct");
                                                            remove(field.name);
                                                            if (correct === idx) form.setFieldValue("correct", 0);
                                                            else if (correct > idx) form.setFieldValue("correct", correct - 1);
                                                        }}
                                                    />
                                                )}
                                            </div>
                                        ))}
                                        {fields.length < 6 && (
                                            <Button type="dashed" icon={<PlusOutlined />} onClick={() => add("")}>
                                                Thêm đáp án
                                            </Button>
                                        )}
                                    </>
                                )}
                            </Form.List>
                        </Radio.Group>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
}
