import {
  ModalState,
  openModal,
  closeModal,
  bindModalOutsideClick,
  getRequiredElements
} from "./modal_common.js";

/**
 * ✅ 날짜 모달 초기화 (flatpickr + 시간 + 정원 - form 모드에만 동작)
 */
export function initDateModal({ onApply } = {}) {
  const ids = {
    btn: "dateBtn",
    modal: "dateModal",
    apply: "dateApply",
    cancel: "dateCancel",
    reset: "dateReset",
    hiddenInput: "dateContainer",
    container: "datePickerContainer"
  };

  const el = getRequiredElements(ids);
  if (!el) return;

  const container = document.getElementById(ids.hiddenInput);
  const pickerContainer = document.getElementById(ids.container);
  const isFormMode = container.dataset.formMode === "true";

  // 임시 input을 만들어 flatpickr 초기화
  const tempInput = document.createElement("input");
  tempInput.type = "text";
  tempInput.style.display = "none";
  pickerContainer.appendChild(tempInput);

  flatpickr.localize(flatpickr.l10ns.ko);
  const fp = flatpickr(tempInput, {
    dateFormat: "Y-m-d",
    locale: "ko",
    mode: "multiple",
    clickOpens: false,
    inline: true,
    appendTo: pickerContainer,

    onDayCreate(_, __, ___, dayElem) {
      const day = dayElem.dateObj.getDay();
      if (day === 0) dayElem.classList.add("sunday");
      else if (day === 6) dayElem.classList.add("saturday");
    },

    onChange(selectedDates) {
      const prevDates = ModalState.getDates();
      const updated = selectedDates.map(d => {
        const dateStr = new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split("T")[0];
        const existing = prevDates.find(p => p.date === dateStr);
        return existing || { date: dateStr, start: "", end: "", capacity: 1 };
      });

      ModalState.setDates(updated);
      renderDateEntries(updated, container, isFormMode);
    }
  });

  el.btn.addEventListener("click", () => {
    openModal(el.modal);
    fp.open();
  });

  el.apply.addEventListener("click", () => {
    if (isFormMode) {
      updateModalStateFromInputs(container); // 🛠️ 적용 버튼 누를 때 상태에 반영
    }
    closeModal(el.modal);
    onApply?.();
  });

  el.cancel.addEventListener("click", () => {
    closeModal(el.modal);
  });

  el.reset.addEventListener("click", () => {
    ModalState.setDates([]);
    fp.clear();
    container.innerHTML = "";
    onApply?.();
  });

  // 🧹 삭제 버튼 동작 연결
  if (isFormMode) {
    container.addEventListener("click", e => {
      const btn = e.target.closest(".remove-date");
      if (!btn) return;

      const dateToRemove = btn.dataset.date;
      const updated = ModalState.getDates().filter(d => d.date !== dateToRemove);
      ModalState.setDates(updated);
      fp.setDate(updated.map(d => d.date), true);
      renderDateEntries(updated, container, isFormMode);
    });
  }

  bindModalOutsideClick(el.modal);
}

/**
 * ✅ 날짜 항목 렌더링 (form 모드에서만 UI 생성됨)
 */
function renderDateEntries(dateEntries, container, isFormMode) {
  if (!container) return;
  container.innerHTML = "";
  if (!isFormMode) return;

  dateEntries.forEach((entry, idx) => {
    const wrapper = document.createElement("div");
    wrapper.className = "date-entry";

    wrapper.innerHTML = `
      <div class="date-label">${entry.date}</div>
      <input type="text" class="timepicker start" name="startTimes[${idx}]" data-index="${idx}" placeholder="시작 시간" value="${entry.start}" required>
      <input type="text" class="timepicker end" name="endTimes[${idx}]" data-index="${idx}" placeholder="종료 시간" value="${entry.end}" required>
      <input type="number" class="capacity" name="capacities[${idx}]" data-index="${idx}" placeholder="정원" value="${entry.capacity}" min="1" required>
      <button type="button" class="remove-date" data-date="${entry.date}">&times;</button>
    `;

    container.appendChild(wrapper);
  });

  // 🕐 flatpickr 적용
  container.querySelectorAll(".timepicker").forEach(el => {
    flatpickr(el, {
      enableTime: true,
      noCalendar: true,
      dateFormat: "H:i",
      time_24hr: true,
      locale: 'ko'
    });
  });

  // 🧠 실시간 ModalState 동기화
  container.querySelectorAll(".date-entry").forEach(entry => {
    const idx = Number(entry.querySelector(".capacity")?.dataset.index);

    ["start", "end", "capacity"].forEach(field => {
      entry.querySelector(`.${field}`).addEventListener("change", () => {
        const updated = ModalState.getDates();
        updated[idx] = {
          ...updated[idx],
          start: entry.querySelector(".start")?.value || "",
          end: entry.querySelector(".end")?.value || "",
          capacity: Number(entry.querySelector(".capacity")?.value || 1)
        };
        ModalState.setDates(updated);
      });
    });
  });
}

/**
 * ✅ 적용 버튼 누를 때 form 입력 → 상태로 수동 반영
 */
function updateModalStateFromInputs(container) {
  const entries = Array.from(container.querySelectorAll(".date-entry"));

  const updated = entries.map(entry => {
    const dateLabel = entry.querySelector(".date-label");
    if (!dateLabel) return null;

    const date = dateLabel.textContent;
    const start = entry.querySelector(".start")?.value || "";
    const end = entry.querySelector(".end")?.value || "";
    const capacity = Number(entry.querySelector(".capacity")?.value || 1);

    return { date, start, end, capacity };
  }).filter(e => e !== null);

  ModalState.setDates(updated);
}

/**
 * ✅ 페이지 내 요소가 전부 있을 경우에만 모달 초기화
 */
export function initDateModalIfExist({ onApply } = {}) {
  const requiredIds = [
    "dateBtn",
    "dateModal",
    "dateApply",
    "dateCancel",
    "dateReset",
    "dateContainer",
    "datePickerContainer"
  ];

  const allExist = requiredIds.every(id => document.getElementById(id));
  if (allExist) {
    initDateModal({ onApply });
  }
}
