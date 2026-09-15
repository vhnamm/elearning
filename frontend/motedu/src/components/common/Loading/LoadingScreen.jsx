import React from "react";
import { Spin } from "antd";
import { LoadingOutlined } from "@ant-design/icons";

export const LoadingScreen = ({ tip = "Đang tải dữ liệu..." }) => {
    // Tùy chọn: dùng icon xoay của Antd Icons nếu muốn tuỳ biến kích cỡ nét vẽ
    const customIcon = <LoadingOutlined style={{ fontSize: 40, color: "var(--color-primary)" }} spin />;

    return (
        <div
            style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                minHeight: "100svh",
                backgroundColor: "var(--bg)",
                color: "var(--text)",
                gap: 16,
            }}
        >
            <Spin indicator={customIcon} size="large" />
            {tip && (
                <span
                    style={{
                        fontFamily: "var(--heading)",
                        fontSize: "15px",
                        color: "var(--text)",
                    }}
                >
          {tip}
        </span>
            )}
        </div>
    );
};

export default LoadingScreen;