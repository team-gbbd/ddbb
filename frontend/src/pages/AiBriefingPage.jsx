import React, {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import "../styles/MainPage.css";
import "../components/AiDashboard.css";
import Header from "../assets/Header";
import MainLogo from "../assets/MainLogo.png";
import api from "../services/management/api";

const initialMessages = {
  mood: "오늘의 베이커리 무드를 준비 중입니다...",
  brief: "예상 판매 요약을 불러오는 중입니다...",
  insight: "재고 인사이트 데이터를 수집 중입니다...",
  strategy: "맞춤형 전략 제안을 계산하고 있습니다...",
};

const errorMessages = {
  mood: "날씨 정보를 불러오지 못했습니다.",
  brief: "판매 요약을 불러오지 못했습니다.",
  insight: "재고 인사이트를 불러오지 못했습니다.",
  strategy: "전략 제안 데이터를 불러오지 못했습니다.",
};

const cards = [
  {
    title: "AI 일일 브리핑",
    icon: "🧠",
    key: "brief",
  },
  {
    title: "재고 인사이트",
    icon: "📦",
    key: "insight",
  },
  {
    title: "전략 제안 / 프로모션",
    icon: "🎯",
    key: "strategy",
  },
];

const AiBriefingPage = () => {
  const [insightData, setInsightData] = useState(initialMessages);
  const [loading, setLoading] = useState(true);
  const [weatherData, setWeatherData] = useState({
    condition: "",
    temperature: null,
  });
  const [weatherLoading, setWeatherLoading] = useState(true);
  const [weatherError, setWeatherError] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);

  const handlePaymentStart = () => {
    window.location.href = "/Payment";
  };

  const handleAdminPage = () => {
    window.location.href = "/admin";
  };

  const loadDashboard = useCallback(() => {
    setLoading(true);
    api
      .get("/dashboard/insights", {
        params: { ts: Date.now() },
      })
      .then((response) => {
        console.log("📊 Dashboard API Response:", response.data);
        const json = response.data || {};
        setInsightData({
          mood: json.mood || initialMessages.mood,
          brief: json.brief || initialMessages.brief,
          insight: json.insight || initialMessages.insight,
          strategy: json.strategy || initialMessages.strategy,
        });
      })
      .catch((err) => {
        console.error("❌ 대시보드 API 호출 실패:", err);
        setInsightData(errorMessages);
      })
      .finally(() => setLoading(false));
  }, []);

  const loadWeather = useCallback(() => {
    setWeatherLoading(true);
    setWeatherError(false);
    api
      .get("/weather/today", {
        params: { ts: Date.now() },
      })
      .then((response) => {
        console.log("🌤️ Weather API Response:", response.data);
        const json = response.data || {};
        setWeatherData({
          condition: json.condition || "",
          temperature:
            typeof json.temperature === "number" ? json.temperature : null,
        });
      })
      .catch((err) => {
        console.error("❌ 날씨 정보 조회 실패:", err);
        setWeatherError(true);
      })
      .finally(() => setWeatherLoading(false));
  }, []);

  const handleReload = useCallback(() => {
    loadDashboard();
    loadWeather();
  }, [loadDashboard, loadWeather]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  useEffect(() => {
    loadWeather();
  }, [loadWeather]);

  useEffect(() => {
    if (cards.length === 0) {
      return;
    }
    const rotationId = setInterval(() => {
      setActiveIndex((prev) => (prev + 1) % cards.length);
    }, 10000);
    return () => clearInterval(rotationId);
  }, []);

  useEffect(() => {
    const refreshId = setInterval(() => {
      handleReload();
    }, 60 * 60 * 1000);
    return () => clearInterval(refreshId);
  }, [handleReload]);

  const timeOfDay = useMemo(() => {
    const hour = new Date().getHours();
    if (hour < 12) return "오전";
    if (hour < 18) return "오후";
    return "저녁";
  }, []);

  const moodMessage = useMemo(() => {
    if (weatherLoading) {
      return initialMessages.mood;
    }

    if (weatherError) {
      return insightData.mood || initialMessages.mood;
    }

    if (weatherData.condition) {
      const temperatureText =
        typeof weatherData.temperature === "number"
          ? `${weatherData.temperature.toFixed(1)}°C`
          : "미확인";
      return `오늘 ${timeOfDay}의 베이커리 무드는 ${weatherData.condition} ☀️ 현재 기온은 ${temperatureText}입니다.`;
    }

    return insightData.mood || initialMessages.mood;
  }, [weatherLoading, weatherError, weatherData, insightData.mood, timeOfDay]);

  const activeCard = cards[activeIndex % cards.length];

  const handleCardClick = useCallback((cardKey) => {
    if (cardKey === "brief") {
      window.location.href = "/admin";
      return;
    }
    if (cardKey === "insight") {
      window.location.href = "/admin/inventory";
      return;
    }
    if (cardKey === "strategy") {
      window.location.href = "/admin/statistics";
    }
  }, []);

  return (
    <>
      <Header />
      <div className="main-page">
        <section className="banner-section ai-briefing-banner">
          <div className="banner-content">
            <div className="banner-image">
              <div className="placeholder-image">
                <img src={MainLogo} alt="빵집 사진" />
              </div>
            </div>
            <div className="banner-text ai-briefing-text">
              <div className="ai-briefing-cards">
                <div className="mood-text-section">
                  <div className="mood-header">
                    <h2>오늘의 AI 브리핑</h2>
                  </div>
                  <p className="mood-text">{moodMessage}</p>
                </div>
                <section className="rotating-card-section">
                  {activeCard && (
                    <div
                      key={activeCard.key}
                      className="ai-card fade-in"
                      onClick={() => handleCardClick(activeCard.key)}
                    >
                      <div className="ai-card-header">
                        <h3>
                          <span className="ai-icon">{activeCard.icon}</span>
                          {activeCard.title}
                        </h3>
                        <button
                          className="ai-refresh"
                          onClick={(event) => {
                            event.stopPropagation();
                            handleReload();
                          }}
                          disabled={loading || weatherLoading}
                        >
                          ↻
                        </button>
                      </div>
                      <p className="ai-text">
                        {loading
                          ? initialMessages[activeCard.key]
                          : insightData[activeCard.key]}
                      </p>
                    </div>
                  )}
                </section>
              </div>
            </div>
          </div>
        </section>

        <section className="action-section">
          <div className="action-container">
            <button
              className="action-button payment"
              onClick={handlePaymentStart}
            >
              <div className="button-icon">💳</div>
              <div className="button-content">
                <h3>결제 시작</h3>
                <p>빠르고 편리한 결제 시작</p>
              </div>
            </button>

            <button className="action-button admin" onClick={handleAdminPage}>
              <div className="button-icon">⚙️</div>
              <div className="button-content">
                <h3>관리자 대시보드</h3>
                <p>재고 및 매출 관리</p>
              </div>
            </button>
          </div>
        </section>
      </div>
    </>
  );
};

export default AiBriefingPage;
