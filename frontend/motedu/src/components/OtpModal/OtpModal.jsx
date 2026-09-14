import { useEffect, useRef, useState } from "react";
import { Modal, Input, Typography, message} from "antd";
import Button from "~components/common/Button/Button";
import styles from "./OtpModal.module.scss";
import {confirmOtp, resendOtp} from "~services/auth.service.js";
import {useNavigate} from "react-router-dom";

const RESEND_SECONDS = 60;

/**
 * Pure UI modal: 6-digit OTP input + resend countdown.
 * All verify/resend network logic is left to the parent via onConfirm/onResend.
 */
const OtpModal = ({
    open,
    ref,
    loading = false,
    resendLoading = false,
    onCancel,
    onConfirm,
    onResend,
}) => {
    const [otp, setOtp] = useState("");
    const [secondsLeft, setSecondsLeft] = useState(RESEND_SECONDS);
    const intervalRef = useRef(null);
    const navigate = useNavigate()

    const startCountdown = () => {
        clearInterval(intervalRef.current);
        setSecondsLeft(RESEND_SECONDS);
        intervalRef.current = setInterval(() => {
            setSecondsLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(intervalRef.current);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    };

    useEffect(() => {
        if (open) {
            setOtp("");
            startCountdown();
        } else {
            clearInterval(intervalRef.current);
        }
        return () => clearInterval(intervalRef.current);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [open]);

    const canResend = secondsLeft === 0 && !resendLoading;

    const handleResend = async () => {
        if (!canResend) return;
        const res = await resendOtp({email: ref?.email, password: ref?.password, fullName: ref?.fullName})
        console.log(res.message)
        message.success(res?.message)
        startCountdown();
    };

    const handleConfirm = async () => {
        if (otp.length !== 6) return;
        try {
            const res = await confirmOtp({email: ref?.email, otp: otp})
            message.success(res?.message)
            navigate('/login')
        }catch (err){
            const errMsg = err.response?.data?.message
            message.error(errMsg)
            setOtp("")
        }


    };

    return (
        <Modal
            open={open}
            onCancel={onCancel}
            footer={null}
            centered
            destroyOnHidden={true}
            className={styles.modal}
        >
            <div className={styles.content}>
                <h3 className={styles.title}>Xác thực OTP</h3>
                <Typography.Paragraph className={styles.subtitle}>
                    Nhập mã gồm 6 chữ số vừa được gửi đến{" "}
                    <strong>{ref?.email}</strong>
                </Typography.Paragraph>

                <Input.OTP
                    length={6}
                    value={otp}
                    onChange={setOtp}
                    size="large"
                    className={styles.otpInput}
                />

                <Button
                    primary
                    rounded
                    size="large"
                    className={styles.confirmBtn}
                    disabled={otp.length !== 6}
                    loading={loading}
                    onClick={handleConfirm}
                >
                    Xác nhận
                </Button>

                <div className={styles.resendRow}>
                    {canResend ? (
                        <button
                            type="button"
                            className={styles.resendLink}
                            onClick={handleResend}
                        >
                            Gửi lại mã
                        </button>
                    ) : (
                        <span className={styles.countdown}>
                            Gửi lại mã sau {secondsLeft}s
                        </span>
                    )}
                </div>
            </div>
        </Modal>
    );
};

export default OtpModal;
