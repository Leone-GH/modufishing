document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".reserve-button").forEach(button => {
    button.addEventListener("click", async () => {
      const postId = button.dataset.postId;
      const availableDate = button.dataset.date;
      const count = 1; // 기본 예약 인원
      const paid = false;

      // ⚠️ userId는 백엔드에서 넘기거나 JS 전역 변수로 전달돼야 함
      const userId = window.currentUserId; // 예시

      const body = {
        reservationPostId: parseInt(postId),
        userId: parseInt(userId),
        availableDate: availableDate,
        count: count,
        paid: paid
      };

      try {
        const response = await fetch("/api/orders", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(body)
        });

        if (response.ok) {
          const result = await response.json();
          alert("예약 성공! 예약 ID: " + result);
          // 결제 페이지로 이동하려면 아래 줄 사용
          // window.location.href = `/payment/${result}`;
        } else {
          const error = await response.text();
          alert("예약 실패: " + error);
        }
      } catch (e) {
        alert("요청 중 오류 발생");
        console.error(e);
      }
    });
  });
});
