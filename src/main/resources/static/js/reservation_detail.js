document.addEventListener("DOMContentLoaded", () => {
  const container = document.body;

  container.addEventListener("click", async (e) => {
    const button = e.target.closest(".reserve-button");
    if (!button) return;

    // 중복 클릭 방지
    if (button.disabled) return;
    button.disabled = true;

    try {
      const postId = parseInt(button.dataset.postId);
      const availableDate = button.dataset.date;
      const userId = parseInt(window.currentUserId); // 서버에서 안전하게 세팅돼야 함
      const count = 1;
      const paid = false;

      // 유효성 체크
      if (!postId || !availableDate || !userId || isNaN(userId)) {
        alert("유효하지 않은 예약 정보입니다. 로그인 상태 또는 날짜를 확인해주세요.");
        return;
      }

      const body = {
        reservationPostId: postId,
        userId: userId,
        availableDate,
        count,
        paid
      };

      const res = await fetch("/api/orders", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        const result = await res.json();
        alert("예약 성공! 예약 ID: " + result);

        // ✅ 이동 경로 필요 시 아래 주석 해제해서 사용
        // window.location.href = `/payment/${result}`;
      } else {
        const error = await res.text();
        alert("예약 실패\n" + (error || "서버 오류가 발생했습니다."));
      }
    } catch (err) {
      console.error("예약 요청 오류:", err);
      alert("요청 중 알 수 없는 오류가 발생했습니다.");
    } finally {
      button.disabled = false;
    }
  });
});
