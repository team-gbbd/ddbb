import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import "../components/AiDashboard.css";

export default function AiDashboard() {
  const [data, setData] = useState({
    mood: null,
    brief: null,
    insight: null,
    strategy: null,
    weather: null,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboard = () => {
    setLoading(true);
    setError(null);

    fetch("/api/dashboard/insights")
      .then((res) => {
        if (!res.ok) throw new Error("API 응답 오류");
        return res.json();
      })
      .then((json) => {
        console.log("📊 Dashboard API Response:", json);
        setData({
          mood: json.mood || "날씨 정보를 가져올 수 없습니다.",
          brief: json.brief || "판매 데이터가 아직 준비되지 않았습니다.",
          insight: json.insight || "재고 데이터가 아직 준비되지 않았습니다.",
          strategy: json.strategy || "전략 제안 데이터를 준비 중입니다.",
          weather: json.weather || null,
        });
        setLoading(false);
      })
      .catch((err) => {
        console.error("❌ 대시보드 API 호출 실패:", err);
        setError("AI 대시보드를 불러오는데 실패했습니다. 서버를 확인해주세요.");
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const getWeatherIcon = () => {
    if (!data.weather) return "☀️";
    const temp = data.weather.temperature;
    const desc = data.weather.description;

    if (desc.includes("비")) return "🌧️";
    if (desc.includes("눈")) return "❄️";
    if (desc.includes("흐림")) return "☁️";
    if (temp < 0) return "🥶";
    if (temp < 10) return "🧣";
    if (temp > 28) return "🔥";
    return "☀️";
  };

  const getWeatherText = () => {
    if (!data.weather) return "날씨와 매장 분위기";
    return `${data.weather.temperature}°C, ${data.weather.description}`;
  };

  const cards = [
    {
      title: "오늘의 베이커리 무드",
      icon: getWeatherIcon(),
      key: "mood",
      color: "#E9F6FF",
      description: getWeatherText(),
    },
    {
      title: "AI 일일 브리핑",
      icon: "🧠",
      key: "brief",
      color: "#FFF0E1",
      description: "실시간 판매 현황",
    },
    {
      title: "재고 인사이트",
      icon: "📦",
      key: "insight",
      color: "#E7F1FF",
      description: "긴급 재고 알림",
    },
    {
      title: "전략 제안",
      icon: "🎯",
      key: "strategy",
      color: "#FFF4B8",
      description: "AI 추천 액션",
    },
  ];

  return (
    <>
      <Header />
      <div className="ai-dashboard-page">
        <div className="ai-dashboard-header">
          <div>
            <h1>🤖 AI 대시보드</h1>
            <p className="subtitle">실시간 데이터 기반 인사이트 (OpenAI GPT-4o-mini 제공)</p>
          </div>
          <button
            className="ai-refresh-btn"
            onClick={fetchDashboard}
            disabled={loading}
          >
            {loading ? "🔄 분석 중..." : "🔄 새로고침"}
          </button>
        </div>

        {error && (
          <div className="ai-error-banner">
            <p>⚠️ {error}</p>
            <button onClick={fetchDashboard}>다시 시도</button>
          </div>
        )}

        <div className="ai-dashboard">
          {cards.map((card) => (
            <div
              key={card.key}
              className={`ai-card ${loading ? "loading" : ""}`}
              style={{ backgroundColor: card.color }}
            >
              <div className="ai-card-header">
                <div className="ai-card-title-section">
                  <span className="ai-icon">{card.icon}</span>
                  <div>
                    <h3>{card.title}</h3>
                    <p className="ai-card-description">{card.description}</p>
                  </div>
                </div>
              </div>
              <div className="ai-card-content">
                {loading ? (
                  <div className="ai-loading-skeleton">
                    <div className="skeleton-line"></div>
                    <div className="skeleton-line short"></div>
                  </div>
                ) : (
                  <p className="ai-text">{data[card.key] || "데이터 없음"}</p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
