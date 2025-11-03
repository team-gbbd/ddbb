import React, { useEffect, useState } from "react";
import api from "../services/management/api";
import "./AiInsightCards.css";

const CARDS = [
  { key: "mood", icon: "☀️", title: "오늘의 베이커리 무드" },
  { key: "briefing", icon: "🧠", title: "AI 일일 브리핑" },
  { key: "inventory", icon: "📦", title: "재고 인사이트" },
  { key: "strategy", icon: "🎯", title: "전략 제안 / 프로모션 Insight" },
];

const INITIAL_DATA = {
  mood: "AI 분석을 준비하고 있습니다...",
  briefing: "판매 데이터를 불러오는 중입니다...",
  inventory: "재고 데이터를 수집 중입니다...",
  strategy: "맞춤 전략을 계산하고 있습니다...",
};

export default function AiInsightCards() {
  const [insights, setInsights] = useState(INITIAL_DATA);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const fetchInsights = async () => {
      setLoading(true);
      setError(null);
      try {
        const { data } = await api.get("/ai/insights");
        if (!cancelled) {
          setInsights({
            mood: data.mood ?? INITIAL_DATA.mood,
            briefing: data.briefing ?? INITIAL_DATA.briefing,
            inventory: data.inventory ?? INITIAL_DATA.inventory,
            strategy: data.strategy ?? INITIAL_DATA.strategy,
          });
        }
      } catch (err) {
        console.error("❌ AI 인사이트 조회 실패:", err);
        if (!cancelled) {
          setError("AI 인사이트를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchInsights();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="ai-insight-cards">
      {CARDS.map((card) => (
        <div className="ai-insight-card" key={card.key}>
          <div className="ai-insight-card__header">
            <span className="ai-insight-card__icon">{card.icon}</span>
            <h3>{card.title}</h3>
          </div>
          <p className="ai-insight-card__body">
            {error
              ? error
              : loading
              ? INITIAL_DATA[card.key]
              : insights[card.key] ?? INITIAL_DATA[card.key]}
          </p>
        </div>
      ))}
    </div>
  );
}
