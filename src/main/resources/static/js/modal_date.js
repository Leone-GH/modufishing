import { selectedDate } from "./modal_state.js";

/**
 * ✅ 날짜 모달 초기화
 * @param {Object} options - 설정 객체
 * @param {Function} options.onApply - 날짜 적용 시 실행할 외부 콜백 함수
 */
export function initDateModal({ onApply } = {}) {
  const dateBtn = document.getElementById("dateBtn");
  const dateModal = document.getElementById("dateModal");
  const dateApply = document.getElementById("dateApply");
  const dateCancel = document.getElementById("dateCancel");
  const dateReset = document.getElementById("dateReset");

  if (!dateBtn || !dateModal || !dateApply || !dateCancel || !dateReset) {
    console.warn("⚠️ [initDateModal] 필수 요소가 없음. HTML 확인 필요.");
    return;
  }

  // 🔘 버튼 클릭 시 모달 열기
  dateBtn.addEventListener("click", () => {
    dateModal.classList.remove("hidden");
    dateModal.classList.add("show");
  });

  // 🔘 날짜 적용 버튼
  dateApply.addEventListener("click", () => {
    closeModal(dateModal);
    if (typeof onApply === "function") onApply();
  });

  // 🔘 닫기 버튼
  dateCancel.addEventListener("click", () => {
    closeModal(dateModal);
  });

  // 🔘 초기화 버튼
  dateReset.addEventListener("click", () => {
    selectedDate.value = []; // ✅ null → 빈 배열로 변경
    if (typeof onApply === "function") onApply();
  });

  // ✅ 외부 클릭 시 모달 닫기
  dateModal.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal")) {
      closeModal(dateModal);
    }
  });

  // ✅ 달력 위젯 초기화
  flatpickr.localize(flatpickr.l10ns.ko);
  flatpickr("#datePickerContainer", {
    dateFormat: "Y-m-d",
    inline: true,
    locale: "ko",
    mode: "multiple", // ✅ 날짜 다중 선택 가능하도록 설정
    onDayCreate: function (dObj, dStr, fp, dayElem) {
      const date = dayElem.dateObj;
      const day = date.getDay(); // 0: 일요일, 6: 토요일

      if (day === 0) {
        dayElem.classList.add("sunday");
      } else if (day === 6) {
        dayElem.classList.add("saturday");
      }
    },
    onChange: (selectedDates, dateStr) => {
      // ✅ 문자열 하나 → 날짜 배열로 저장
      selectedDate.value = selectedDates.map(d => d.toISOString().split("T")[0]);
    },
    appendTo: document.getElementById("datePickerContainer")
  });
}

// ✅ 모달 닫기 함수
function closeModal(modal) {
  modal.classList.remove("show");
  modal.classList.add("hidden");
}

/**
 * ✅ 조건부 초기화 (버튼 존재 시만)
 * 기본 초기화만 필요할 경우 사용
 */
export function initDateModalIfExist() {
  const dateBtn = document.getElementById("dateBtn");
  if (dateBtn) initDateModal();
}
