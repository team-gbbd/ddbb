import "./App.css";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import MainPage from "./pages/MainPage";
import GuidePage from "./pages/GuidePage";
import Payment from "./pages/Payment";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/guide" element={<GuidePage />} />
        <Route path="/Payment" element={<Payment />} />
      </Routes>
    </Router>
  );
}

export default App;
