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
        padding: "12px 32px",
        borderBottom: "1px solid #eee",
      }}
    >
      <Link to="/">
        <img src={ddbblogo} alt="ddbblogo" style={{ height: 40 }} />
      </Link>
      <Link
        to="/guide"
        style={{
          color: "#222",
          textDecoration: "none",
          fontWeight: 500,
          fontSize: 16,
        }}
      >
        이용가이드
      </Link>
    </header>
  );
};

export default Header;
