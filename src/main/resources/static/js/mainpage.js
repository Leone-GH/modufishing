function navigatePage(url) {
  window.location.href = url;
}

document.addEventListener("DOMContentLoaded", function () {
  // Lottie 애니메이션
  lottie.loadAnimation({
    container: document.getElementById('lottiePopularPosts'),
    renderer: 'svg',
    loop: true,
    autoplay: true,
    path: '/json/mascot.json'
  });

  gsap.registerPlugin(ScrollTrigger);

  // 초기 상태를 명확하게 지정 (style 속성에 강제 주입)
  gsap.set("#comImage", {
    opacity: 0,
    scale: 1.2,
    filter: "blur(6px)"
  });

  // fromTo 애니메이션
  gsap.to("#comImage", {
    opacity: 1,
    scale: 1,
    filter: "blur(0px)",
    duration: 1.5,
    scrollTrigger: {
      trigger: ".catchLogSection",
      start: "top 80%",
      end: "bottom top",
      toggleActions: "play none none reverse", // scrub 제거
    }
  });
});
