import { useLocation } from "react-router-dom";
import { useEffect } from "react";
import React from "react";

function ScrollToTop() {
  const location = useLocation();

  useEffect(() => {
    if ('scrollRestoration' in window.history) {
    window.history.scrollRestoration = 'manual';
  }
    window.scrollTo(0, 0);
    
  }, [location.pathname, location.search]);

  return null;
}

export default ScrollToTop;
