import React from "react";
import { Link } from "react-router-dom";
import ddbblogo from "../assets/ddbblogo.png";

const Header = () => {
  return (
    <header
      style={{
        background: "#fff",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "14px 32px",
        borderBottom: "1px solid #eee",
      }}
    >
      <Link to="/">
        <img src={ddbblogo} alt="ddbblogo" style={{ height: 45 }} />
      </Link>
      <Link
        to="/guide"
        style={{
          color: "#222",
          textDecoration: "none",
          fontWeight: 600,
          fontSize: 16,
        }}
      >
        이용가이드
      </Link>
    </header>
  );
};

export default Header;
