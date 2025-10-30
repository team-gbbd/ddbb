import "./App.css";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import MainPage from "./pages/MainPage";
import GuidePage from "./pages/GuidePage";
import Payment from "./pages/Payment";
import AdminDashboard from "./pages/management/AdminDashboard";
import Inventory from "./pages/management/Inventory";
import Statistics from "./pages/management/Statistics";
import AIAnalysis from "./pages/management/AIAnalysis";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/guide" element={<GuidePage />} />
        <Route path="/Payment" element={<Payment />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/inventory" element={<Inventory />} />
        <Route path="/admin/statistics" element={<Statistics />} />
        <Route path="/admin/ai-analysis" element={<AIAnalysis />} />
      </Routes>
    </Router>
  );
}

export default App;
