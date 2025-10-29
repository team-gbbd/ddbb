import "./App.css";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import MainPage from "./pages/MainPage";
import GuidePage from "./pages/GuidePage";
import Payment from "./pages/Payment";
import Admin from "./pages/Admin";


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/GuidePage" element={<GuidePage />} />
        <Route path="/Payment" element={<Payment />} />
        <Route path="/Admin" element={<Admin />} />
      </Routes>
    </Router>
  );
}

export default App;
