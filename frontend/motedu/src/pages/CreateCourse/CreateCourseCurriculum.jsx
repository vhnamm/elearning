import { useEffect, useRef, useState } from "react";
import { useOutletContext, useParams } from "react-router-dom";
import { Button, Dropdown, Form, Input, Modal, Progress, Radio, Spin, Switch, Tooltip, message } from "antd";
import {getInstructorCurriculum, createNewSection, deleteSection as deleteSectionApi, reorderSection, createCurriculumItem,
    getContentPresignedUrl, putToStorage, completeContentUpload, getContentAccess, updateCurriculumItem,
    updateSection as updateSectionApi, deleteCurriculumItem

} from "~services/course.service.js";

import {createQuestion, deleteQuestion as deleteQuestionApi, getQuestions, updateQuestion} from "~services/quiz.service.js";

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
import axios from "axios";

const formatDuration = (seconds) => {
    const total = Math.max(0, Math.round(seconds || 0));
    const m = String(Math.floor(total / 60)).padStart(2, "0");
    const s = String(total % 60).padStart(2, "0");
    return `${m}:${s}`;
};

/**
 * Đọc thời lượng + chụp 1 khung hình làm thumbnail từ file video cục bộ.
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
        video.onerror = () => done({ duration: 0, thumbnailBlob: null });
        video.onloadedmetadata = () => {
            const duration = video.duration;
            video.currentTime = Math.min(Math.max(0.1, duration / 2), 1);

            video.onseeked = () => {
                try {
                    const canvas = document.createElement("canvas");
                    canvas.width = 320;
                    canvas.height = 180;
                    canvas.getContext("2d").drawImage(video, 0, 0, 320, 180);
                    canvas.toBlob(
                        (blob) => done({ duration, thumbnailBlob: blob }),
                        "image/jpeg",
                        0.8
                    );

                } catch {
                    done({ duration, thumbnailBlob: null });
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

// Đổi câu hỏi từ dạng BE (đáp án là object có isCorrect) sang dạng form/state của FE (mảng chuỗi + chỉ số đúng).
const toFormQuestion = (q) => ({
    id: q.id,
    text: q.content,
    options: q.options.map((o) => ({ id: o.id, content: o.content })),
    correct: q.options.findIndex((o) => o.isCorrect),
});

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
    const [addingItem, setAddingItem] = useState(null); // { sectionId, type }
    const [newItemTitle, setNewItemTitle] = useState("");
    const [creatingItem, setCreatingItem] = useState(false);
    const [uploads, setUploads] = useState({}) //itemId: {percent, status}
    const [videoPreview, setVideoPreview] = useState(null); // { url, title }
    const dragRef = useRef(null); // { kind: "section" | "item", sectionId, id }
    const titleBackupRef = useRef(""); // tên item lúc bắt đầu sửa, để trả lại nếu để trống hoặc lưu lỗi

    const togglePreview = async (sectionId, itemId, checked) => {
        updateItem(sectionId, itemId, { preview: checked }); // đổi UI ngay
        try {
            await updateCurriculumItem(itemId, { preview: checked });
        } catch (err) {
            updateItem(sectionId, itemId, { preview: !checked }); // lỗi thì trả lại
            message.error(err?.response?.data?.message ?? "Không thể cập nhật xem trước");
        }
    };

    const saveSectionTitle = async (section, rawTitle) => {
        setEditingSectionId(null);
        const title = rawTitle.trim();
        if (!title || title === section.title) return; // để trống hoặc không đổi thì giữ tên cũ
        try {
            await updateSectionApi(courseId, versionId, section.id, { title });
            updateSection(section.id, (s) => ({ ...s, title }));
        } catch (err) {
            message.error(err?.response?.data?.message ?? "Không thể đổi tên phần");
        }
    };

    const saveItemTitle = async (sectionId, item) => {
        const title = (item.title ?? "").trim();
        const old = titleBackupRef.current;
        if (title === old) return;
        if (!title) {
            updateItem(sectionId, item.id, { title: old });
            message.warning("Tên bài học không được để trống");
            return;
        }
        try {
            await updateCurriculumItem(item.id, { title });
            updateItem(sectionId, item.id, { title });
        } catch (err) {
            updateItem(sectionId, item.id, { title: old });
            message.error(err?.response?.data?.message ?? "Không thể đổi tên bài học");
        }
    };
    const fileInputRef = useRef(null);
    const pendingUploadRef = useRef(null); // { sectionId, itemId }
    const [savingQuestion, setSavingQuestion] = useState(false)
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
    const openAddItem = (sectionId, type) => {
        setNewItemTitle("");
        setAddingItem({ sectionId, type });
    };

    const cancelAddItem = () => {
        setAddingItem(null);
        setNewItemTitle("");
    };

    const onCreateItem = async () => {
        const title = newItemTitle.trim();
        if (!title) {
            message.warning("Vui lòng nhập tiêu đề cho bài học này");
            return;
        }
        const { sectionId, type } = addingItem;
        setCreatingItem(true);
        try {
            const result = await createCurriculumItem(sectionId, { type, title });
            // Quiz mới chưa có câu hỏi: gán sẵn danh sách rỗng và mở rộng luôn để thấy nút "Thêm câu hỏi mới".
            const created = type === "QUIZ" ? { ...result.data, questions: [] } : result.data;
            updateSection(sectionId, (s) => ({ ...s, curriculumItems: [...s.curriculumItems, created] }));
            if (type === "QUIZ") setExpanded((prev) => ({ ...prev, [created.id]: true }));
            cancelAddItem();
        } catch {
            message.error("Không thể tạo bài học mới");
        } finally {
            setCreatingItem(false);
        }
    };

    const deleteItem = (sectionId, item) =>
        Modal.confirm({
            title: "Xóa bài học này?",
            content: `Bài học "${item.title}" và nội dung của nó sẽ bị xóa.`,
            okText: "Xóa",
            cancelText: "Hủy",
            okButtonProps: { danger: true },
            onOk: async () => {
                try {
                    await deleteCurriculumItem(sectionId, item.id);
                    updateSection(sectionId, (s) => ({
                        ...s,
                        curriculumItems: s.curriculumItems.filter((i) => i.id !== item.id),
                    }));
                    message.success("Đã xóa bài học");
                } catch (err) {
                    message.error(err?.response?.data?.message ?? "Không thể xóa bài học");
                }
            },
        });

    const toggleExpand = async (sectionId, item) => {
        const opening = !expanded[item.id];
        setExpanded((prev) => ({ ...prev, [item.id]: opening }));

        // Quiz chưa có danh sách câu hỏi trong state (vừa F5) thì tải từ BE, mỗi item chỉ tải một lần.
        if (opening && item.type === "QUIZ" && !item.questions) {
            try {
                const data = await getQuestions(item.id);
                updateItem(sectionId, item.id, { questions: data.questions.map(toFormQuestion) });
            } catch {
                message.error("Không thể tải danh sách câu hỏi");
            }
        }
    };

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
        const { sectionId, itemId, kind } = target;

        setUploads((prev) => ({...prev, [itemId]: {percent: 0}}))
        try {
            //0. Video thi phai cat ra lay thumb
            let metadata = null
            if(kind == "VIDEO") metadata = await readVideoMeta(file);
            const thumbBlob = metadata?.thumbnailBlob ?? null;

            //1. Xin presigned url chung cho ca 2 loai VIDEO va DOC
            const presigned = await getContentPresignedUrl(itemId, {
                kind: kind,
                contentType: file.type,
                fileName: file.name,
                fileSizeBytes: file.size,
                thumbnailContentType: thumbBlob ? "image/jpeg" : undefined
            })

            //2. PUT len R2
            if(thumbBlob && presigned.thumbnailUploadUrl){
                await putToStorage(presigned.thumbnailUploadUrl, thumbBlob)
            }
            await putToStorage(presigned.uploadUrl, file, (percent) => setUploads(
                (prev) => ({...prev, [itemId]: { percent: percent }}))
            )

            //3.goi api complete
            await completeContentUpload(itemId, {
                durationSeconds: Math.round(metadata?.duration ?? 0)
            })

            //4.Cap nhat UI
            if(kind == "VIDEO"){
                updateItem(sectionId, itemId, {
                    contentType: "VIDEO",
                    videoStatus: "READY",
                    fileName: file.name,
                    videoDurationSeconds: metadata.duration,
                    thumbnailUrl: thumbBlob ? URL.createObjectURL(thumbBlob) : null
                })
            }else{
                updateItem(sectionId, itemId, {
                    contentType: "DOCUMENT",
                    fileName: file.name,
                    fileSizeBytes: file.size,
                    fileUrl: URL.createObjectURL(file),
                });
            }
            setExpanded((prev) => ({ ...prev, [target.itemId]: true }));
        }catch (err){
            if (axios.isCancel(err)) message.info("Đã hủy tải lên");
            else message.error(err?.response?.data?.message ?? "Tải lên thất bại, vui lòng thử lại");
        }finally {
            setUploads((prev) => {
                const next = { ...prev };
                delete next[itemId];
                return next;
            });
        }
    };

    const previewDocument = async (item) => {
        // Mở tab trống ngay trong lúc bấm, nếu chờ API xong mới mở thì trình duyệt chặn popup.
        const tab = window.open("about:blank", "_blank");
        try {
            const access = await getContentAccess(item.id);
            if (tab) tab.location.href = access.accessUrl;
        } catch (err) {
            tab?.close();
            message.error(err?.response?.data?.message ?? "Không thể xem trước tệp này");
        }
    };

    // Link có hạn nên không lưu lâu, mỗi lần bấm "Xem trước" xin link mới.
    const previewVideo = async (item) => {
        try {
            const access = await getContentAccess(item.id);
            setVideoPreview({ url: access.accessUrl, title: item.title });
        } catch (err) {
            message.error(err?.response?.data?.message ?? "Không thể xem trước video này");
        }
    };


    const openQuestionModal = (sectionId, itemId, question = null) => {
        form.setFieldsValue(
            question
                ? { text: question.text, options: question.options, correct: question.correct }
                : { text: "", options: [{ id: null, content: "" }, { id: null, content: "" }], correct: 0 },
        );
        setQuestionModal({ sectionId, itemId, question });
    };

    const handleCreateQuestion = async () => {
        const values = await form.validateFields();
        const { sectionId, itemId } = questionModal;

        const body = {
            content: values.text.trim(),
            options: values.options.map((opt, i) => ({
                id: null,
                content: (typeof opt === "string" ? opt : opt.content).trim(),
                isCorrect: i === values.correct,
            })),
        };

        setSavingQuestion(true);
        try {
            const saved = await createQuestion(itemId, body);
            const payload = toFormQuestion(saved.data ?? saved);

            updateSection(sectionId, (s) => ({
                ...s,
                curriculumItems: s.curriculumItems.map((i) => {
                    if (i.id !== itemId) return i;
                    return { ...i, questions: [...(i.questions || []), payload] };
                }),
            }));

            message.success("Thêm câu hỏi thành công");
            setQuestionModal(null);
        } catch (err) {
            message.error(err?.response?.data?.message ?? "Không thể tạo câu hỏi");
        } finally {
            setSavingQuestion(false);
        }
    };

    // Hàm 2: Xử lý CẬP NHẬT
    const handleUpdateQuestion = async () => {
        const values = await form.validateFields();
        const { sectionId, itemId, question } = questionModal;

        const body = {
            content: values.text.trim(),
            options: values.options.map((opt, i) => ({
                id: opt?.id ?? null,
                content: (typeof opt === "string" ? opt : opt.content).trim(),
                isCorrect: i === values.correct,
            })),
        };

        setSavingQuestion(true);
        try {
            const updated = await updateQuestion(itemId ,question.id, body);
            const payload = toFormQuestion(updated.data ?? updated);

            updateSection(sectionId, (s) => ({
                ...s,
                curriculumItems: s.curriculumItems.map((i) => {
                    if (i.id !== itemId) return i;
                    const questions = (i.questions || []).map((q) => (q.id === question.id ? payload : q));
                    return { ...i, questions };
                }),
            }));

            message.success("Cập nhật câu hỏi thành công");
            setQuestionModal(null);
        } catch (err) {
            message.error(err?.response?.data?.message ?? "Không thể cập nhật câu hỏi");
        } finally {
            setSavingQuestion(false);
        }
    };

    const deleteQuestion = (sectionId, itemId, question) =>
        Modal.confirm({
            title: "Xóa câu hỏi này?",
            content: `Câu hỏi "${question.text}" và các đáp án của nó sẽ bị xóa.`,
            okText: "Xóa",
            cancelText: "Hủy",
            okButtonProps: { danger: true },
            onOk: async () => {
                try {
                    await deleteQuestionApi(itemId, question.id);
                    updateSection(sectionId, (s) => ({
                        ...s,
                        curriculumItems: s.curriculumItems.map((i) =>
                            i.id !== itemId
                                ? i
                                : { ...i, questions: (i.questions || []).filter((q) => q.id !== question.id) },
                        ),
                    }));
                    message.success("Đã xóa câu hỏi");
                } catch (err) {
                    message.error(err?.response?.data?.message ?? "Không thể xóa câu hỏi");
                }
            },
        });

    /* ---------- drag & drop (HTML5 native) ---------- */
    const dropSection = async (targetId) => {
        const drag = dragRef.current;
        dragRef.current = null;
        if (drag?.kind !== "section") return;

        const from = sections.findIndex((s) => s.id === drag.id);
        const to = sections.findIndex((s) => s.id === targetId);
        if (from === -1 || to === -1 || from === to) return;

        const prev = sections;
        const next = reorder(prev, from, to);
        setSections(next);                       // cập nhật UI ngay (optimistic)

        try {
            await reorderSection(courseId, versionId, {sectionIds: next.map((s) => s.id)});
        } catch {
            setSections(prev);                   // lỗi thì trả về thứ tự cũ
            message.error("Không thể thay đổi thứ tự phần");
        }
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
                            <div className={styles.questionEdit}>
                                <Button
                                    size="small"
                                    icon={<EditOutlined />}
                                    onClick={() => openQuestionModal(section.id, item.id, q)}
                                >
                                    Chỉnh sửa
                                </Button>
                                <Button
                                    size="small"
                                    danger
                                    icon={<DeleteOutlined />}
                                    onClick={() => deleteQuestion(section.id, item.id, q)}
                                >
                                    Xóa
                                </Button>
                            </div>
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
                        <Button
                            size="small"
                            disabled={!!uploads[item.id]}
                            onClick={() => pickFile(section.id, item.id, "DOCUMENT")}
                        >
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
                    <Button
                        size="small"
                        disabled={!!uploads[item.id]}
                        onClick={() => pickFile(section.id, item.id, "VIDEO")}
                    >
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
                                onFocus={() => (titleBackupRef.current = item.title)}
                                onChange={(e) => updateItem(section.id, item.id, { title: e.target.value })}
                                onBlur={() => saveItemTitle(section.id, item)}
                                onPressEnter={(e) => e.currentTarget.blur()}
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
                                        onChange={(checked) => togglePreview(section.id, item.id, checked)}
                                    />
                                    <span>Xem trước</span>
                                </span>
                            </Tooltip>
                        )}
                        {item.type === "LECTURE" && !hasContent(item) && (
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
                        )}
                        <Button
                            type="text"
                            size="small"
                            icon={<DeleteOutlined />}
                            className={styles.iconBtn}
                            onClick={() => deleteItem(section.id, item)}
                            disabled={!!uploads[item.id]}
                            aria-label="Xóa bài học"
                        />
                        {(hasContent(item) || item.type === "QUIZ") && (
                            <Button
                                type="text"
                                size="small"
                                className={styles.iconBtn}
                                icon={isOpen ? <CaretDownOutlined /> : <CaretRightOutlined />}
                                onClick={() => toggleExpand(section.id, item)}
                                aria-label={isOpen ? "Thu gọn" : "Mở rộng"}
                            />
                        )}
                    </div>
                </div>
                {uploads[item.id] && (
                    <Progress percent={uploads[item.id].percent} size="small" style={{ padding: "0 12px" }} />
                )}
                {(hasContent(item) || item.type === "QUIZ") && isOpen && <div className={styles.itemExpanded}>{renderItemBody(section, item)}</div>}
            </div>
        );
    };

    const addMenu = (sectionId) => ({
        items: [
            { key: "LECTURE", icon: <PlayCircleOutlined />, label: "Bài giảng (Lecture)" },
            { key: "QUIZ", icon: <FileTextOutlined />, label: "Bài trắc nghiệm (Quiz)" },
        ],
        onClick: ({ key }) => openAddItem(sectionId, key),
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
                                        onBlur={(e) => saveSectionTitle(section, e.target.value)}
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
                                {addingItem?.sectionId === section.id ? (
                                    <div className={styles.addSectionForm}>
                                        <Input
                                            autoFocus
                                            maxLength={255}
                                            value={newItemTitle}
                                            placeholder={
                                                addingItem.type === "QUIZ"
                                                    ? "Nhập tên bài trắc nghiệm"
                                                    : "Nhập tên bài giảng"
                                            }
                                            onChange={(e) => setNewItemTitle(e.target.value)}
                                            onPressEnter={onCreateItem}
                                            onKeyDown={(e) => e.key === "Escape" && cancelAddItem()}
                                        />
                                        <Button onClick={cancelAddItem} disabled={creatingItem}>
                                            Hủy
                                        </Button>
                                        <Button type="primary" loading={creatingItem} onClick={onCreateItem}>
                                            Thêm
                                        </Button>
                                    </div>
                                ) : (
                                    <Dropdown menu={addMenu(section.id)} trigger={["click"]}>
                                        <Button icon={<PlusOutlined />}>Thêm bài giảng / bài tập</Button>
                                    </Dropdown>
                                )}
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
                open={!!videoPreview}
                title={videoPreview?.title}
                footer={null}
                width={800}
                onCancel={() => setVideoPreview(null)}
                destroyOnHidden
            >
                {videoPreview && <video src={videoPreview.url} controls autoPlay style={{ width: "100%" }} />}
            </Modal>

            <Modal
                open={!!questionModal}
                title={questionModal?.question ? "Chỉnh sửa câu hỏi" : "Thêm câu hỏi mới"}
                cancelText="Hủy"
                confirmLoading={savingQuestion}
                onCancel={() => setQuestionModal(null)}
                destroyOnHidden
                forceRender
                footer={[
                    <Button key="cancel" onClick={() => setQuestionModal(null)}>
                        Hủy
                    </Button>,

                    // NÚT 1: Chỉ hiện khi ĐANG SỬA
                    questionModal?.question ? (
                        <Button
                            key="update"
                            type="primary"
                            loading={savingQuestion}
                            onClick={handleUpdateQuestion}
                        >
                            Cập nhật
                        </Button>
                    ) : (
                        // NÚT 2: Chỉ hiện khi TẠO MỚI
                        <Button
                            key="create"
                            type="primary"
                            loading={savingQuestion}
                            onClick={handleCreateQuestion}
                        >
                            Tạo mới
                        </Button>
                    ),
                ]}
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
                                                <Form.Item name={[field.name, "id"]} hidden noStyle>
                                                    <Input type="hidden" />
                                                </Form.Item>
                                                <Form.Item
                                                    name={[field.name, "content"]}
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
                                            <Button type="dashed" icon={<PlusOutlined />} onClick={() => add({ id: null, content: "" })}>
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
