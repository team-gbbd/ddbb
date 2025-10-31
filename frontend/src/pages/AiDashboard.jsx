import React, { useEffect, useState } from "react";
import "../components/AiDashboard.css";

export default function AiDashboard() {
  const [data, setData] = useState({
    mood: null,
    brief: null,
    insight: null,
    strategy: null,
  });

  useEffect(() => {
    fetch("http://localhost:8080/api/dashboard/insights")
      .then((res) => res.json())
      .then((json) => {
        console.log("📊 Dashboard API Response:", json);
        setData({
          mood: json.mood || "날씨 정보를 가져올 수 없습니다.",
          brief: json.brief || "판매 데이터가 아직 준비되지 않았습니다.",
          insight: json.insight || "재고 데이터가 아직 준비되지 않았습니다.",
          strategy: json.strategy || "전략 제안 데이터를 준비 중입니다.",
        });
      })
      .catch((err) => {
        console.error("❌ 대시보드 API 호출 실패:", err);
        setData({
          mood: "날씨 API 오류 발생",
          brief: "판매 요약을 불러오지 못했습니다.",
          insight: "재고 인사이트를 불러오지 못했습니다.",
          strategy: "전략 제안 데이터를 불러오지 못했습니다.",
        });
      });
  }, []);

  const cards = [
    {
      title: "오늘의 베이커리 무드",
      icon: "☀️",
      key: "mood",
      color: "#E9F6FF",
    },
    {
      title: "AI 일일 브리핑",
      icon: "🧠",
      key: "brief",
      color: "#FFF0E1",
    },
    {
      title: "재고 인사이트",
      icon: "📦",
      key: "insight",
      color: "#E7F1FF",
    },
    {
      title: "전략 제안 / 프로모션 Insight",
      icon: "🎯",
      key: "strategy",
      color: "#FFF4B8",
    },
  ];

  return (
    <div className="ai-dashboard">
      {cards.map((card) => (
        <div
          key={card.key}
          className="ai-card"
          style={{ backgroundColor: card.color }}
        >
          <div className="ai-card-header">
            <h3>
              <span className="ai-icon">{card.icon}</span>
              {card.title}
            </h3>
            <button
              className="ai-refresh"
              onClick={() => window.location.reload()}
            >
              ↻
            </button>
          </div>
          <p className="ai-text">
            {data[card.key] || "AI 분석 대기 중..."}
          </p>
        </div>
      ))}
    </div>
  );
}
