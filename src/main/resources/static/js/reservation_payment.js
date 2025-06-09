document.addEventListener("DOMContentLoaded", function () {
  console.log("✅ JS 파일 실행됨");

  const infoEl = document.getElementById("reservation-info");
  if (!infoEl) {
    console.error("❌ 예약 정보 엘리먼트 없음");
    return;
  }

  const data = {
    postId: parseInt(infoEl.dataset.postId),
    userId: parseInt(infoEl.dataset.userId),
    date: infoEl.dataset.date,
    count: parseInt(infoEl.dataset.count),
    price: parseInt(infoEl.dataset.price)
  };

  console.log("✅ 파싱된 예약 데이터:", data);
  window.reservationData = data;

  const payBtn = document.getElementById("pay-btn");
  const IMP = window.IMP;
  IMP.init("imp47607357");

  if (!payBtn || !IMP) {
    console.error("❌ 초기화 실패");
    return;
  }

  payBtn.addEventListener("click", function () {
    if (!data || !data.postId || !data.price || !data.count) {
      alert("❌ 예약 정보가 불완전합니다.");
      return;
    }

    const amount = data.price * data.count;
    if (isNaN(amount) || amount <= 0) {
      alert("❌ 결제 금액이 유효하지 않습니다.");
      return;
    }

    IMP.request_pay({
      pg: "kakaopay",
      channelKey: "channel-key-925249c1-1a80-4645-850d-218e18cea0ad",
      merchant_uid: "order_" + new Date().getTime(),
      name: "FishTrip 예약 결제",
      amount: amount,
      buyer_email: "test@example.com",
      buyer_name: "홍길동",
      buyer_tel: "010-1234-5678",
      buyer_addr: "서울특별시 어딘가",
      buyer_postcode: "12345",
      m_redirect_url: "http://localhost:8090/payment/verify"
    }, function (rsp) {
      if (rsp.success) {
        window.location.href = "/payment/verify"
          + "?imp_uid=" + rsp.imp_uid
          + "&merchant_uid=" + rsp.merchant_uid
          + "&postId=" + data.postId
          + "&userId=" + data.userId
          + "&date=" + data.date
          + "&count=" + data.count;
      } else {
        alert("결제 실패: " + rsp.error_msg);
      }
    });
  });
});
