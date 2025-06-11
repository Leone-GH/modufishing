console.log("✅ reservation_detail.js 시작됨");

document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ DOMContentLoaded 이벤트 발생");

  const container = document.body;

  // ✅ 요일 글자 스타일링 (기존 그대로)
  document.querySelectorAll(".date-text").forEach(el => {
    const original = el.textContent;
    const match = original.match(/(.+)\((.)\)/);
    if (!match) return;

    const [_, datePart, dayChar] = match;
    let className = "";
    if (dayChar === "토") className = "flatpickr-day saturday";
    else if (dayChar === "일") className = "flatpickr-day sunday";

    if (className) {
      el.innerHTML = `${datePart}(<span class="${className}">${dayChar}</span>)`;
    } else {
      el.innerHTML = original;
    }
  });

  // ✅ 예약 버튼 → 결제 페이지로 이동
  container.addEventListener("click", (e) => {
    const button = e.target.closest(".reserve-button");
    if (!button) return;

    const postId = parseInt(button.dataset.postId);
    const availableDate = button.dataset.date;
    const countInput = button.parentElement.querySelector(".reserve-count");
    const count = parseInt(countInput.value);

    console.log("postId:", postId);
    console.log("availableDate:", availableDate);
    console.log("count:", count);

    if (!postId || !availableDate || isNaN(count) || count < 1) {
      alert("예약 정보가 유효하지 않습니다.");
      return;
    }

    // 결제 페이지로 이동
    const url = `/reservation/payment?postId=${postId}&date=${availableDate}&count=${count}`;
    window.location.href = url;
  });
});



