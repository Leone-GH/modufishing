// reservation_detail.js (🔥 모달 hidden 처리 + Fade 효과 적용 최종 완성본)
document.addEventListener("DOMContentLoaded", function () {
  let cachedRegions = null;
  let selectedRegions = [];

  const regionBtn = document.getElementById("regionBtn");
  const regionModal = document.getElementById("regionModal");
  const regionList = document.getElementById("regionList");
  const regionApply = document.getElementById("regionApply");
  const regionReset = document.getElementById("regionReset");

  regionBtn.addEventListener("click", () => {
    if (!regionModal || !regionList) return;

    regionModal.classList.remove("hidden"); // ✅ hidden 제거
    regionModal.classList.add("show");       // ✅ Fade In 적용

    if (cachedRegions) {
      renderFilteredRegions(cachedRegions, regionList);
      return;
    }

    fetch("/api/regions/hierarchy")
      .then(res => res.ok ? res.json() : Promise.reject(res))
      .then(data => {
        cachedRegions = data;
        renderFilteredRegions(data, regionList);
      })
      .catch(err => {
        console.error("데이터 로딩 실패:", err);
        regionList.innerHTML = '<p style="color:red;">지역 데이터를 불러오는 데 실패했습니다.</p>';
      });
  });

  regionApply.addEventListener("click", () => {
    regionModal.classList.remove("show");   // ✅ Fade Out
    regionModal.classList.add("hidden");     // ✅ hidden 다시 추가
    fetchFilteredCards();
  });

  regionReset.addEventListener("click", () => {
    selectedRegions = [];
    updateSelectedRegionText();
    document.querySelectorAll(".region-child-btn.selected").forEach(btn => btn.classList.remove("selected"));
  });

  function renderFilteredRegions(data, container) {
    container.innerHTML = '';
    data.forEach(region => {
      const parentWrapper = document.createElement('div');
      parentWrapper.classList.add('region-group');

      const title = document.createElement('div');
      title.innerText = region.name;
      title.classList.add('region-title');
      parentWrapper.appendChild(title);

      const childWrapper = document.createElement('div');
      childWrapper.classList.add('region-children');

      const allBtn = document.createElement('button');
      allBtn.innerText = '전체';
      allBtn.classList.add('region-child-btn');

      allBtn.addEventListener("click", () => {
        const childBtnList = Array.from(childWrapper.querySelectorAll('.region-child-btn:not(:first-child)'));
        const alreadySelected = childBtnList.every(btn => btn.classList.contains("selected"));

        if (alreadySelected) {
          selectedRegions = selectedRegions.filter(r => r.parent !== region.name);
          allBtn.classList.remove("selected");
          childBtnList.forEach(btn => btn.classList.remove("selected"));
        } else {
          selectedRegions = selectedRegions.filter(r => r.parent !== region.name);

          region.children.forEach(child => {
            selectedRegions.push({ id: child.id, name: child.name, parent: region.name });
          });

          childBtnList.forEach(btn => btn.classList.add("selected"));
          allBtn.classList.add("selected");
        }

        updateSelectedRegionText();
      });

      childWrapper.appendChild(allBtn);

      region.children.forEach(child => {
        const btn = document.createElement('button');
        btn.innerText = child.name;
        btn.classList.add('region-child-btn');
        btn.dataset.regionId = child.id;

        btn.addEventListener("click", () => {
          const id = child.id;
          const existingIndex = selectedRegions.findIndex(r => r.id === id);

          btn.classList.toggle("selected");

          if (existingIndex !== -1) {
            selectedRegions.splice(existingIndex, 1);
          } else {
            selectedRegions.push({ id, name: child.name, parent: region.name });
          }

          const totalSelected = region.children.every(c =>
            selectedRegions.some(r => r.id === c.id)
          );

          if (totalSelected) {
            selectedRegions = selectedRegions.filter(r => r.parent !== region.name);
            region.children.forEach(child => {
              selectedRegions.push({ id: child.id, name: child.name, parent: region.name });
            });

            allBtn.classList.add("selected");
            childWrapper.querySelectorAll('.region-child-btn:not(:first-child)').forEach(b => {
              b.classList.add("selected");
            });
          } else {
            selectedRegions = selectedRegions.filter(r => !(r.parent === region.name && r.name === "전체"));
            allBtn.classList.remove("selected");
          }

          updateSelectedRegionText();
        });

        childWrapper.appendChild(btn);
      });

      parentWrapper.appendChild(childWrapper);
      container.appendChild(parentWrapper);
    });
  }

  function updateSelectedRegionText() {
    const modalSelectionDiv = document.querySelector("#regionModal .current-selection");
    const pageSelectionDiv = document.getElementById("selectedInfo");

    if (selectedRegions.length === 0) {
      modalSelectionDiv.innerText = "선택된 지역 없음";
      pageSelectionDiv.innerText = "";
      return;
    }

    let grouped = {};
    selectedRegions.forEach(r => {
      if (!grouped[r.parent]) grouped[r.parent] = [];
      grouped[r.parent].push(r.name);
    });

    const regionTexts = Object.entries(grouped).map(([parent, names]) => {
      const totalChildCount = cachedRegions.find(r => r.name === parent)?.children.length ?? 0;
      if (names.length === totalChildCount) {
        return `(${parent}) 전체`;
      } else {
        return `(${parent}) ${names.join(', ')}`;
      }
    });

    const finalText = `현재 선택 지역: ${regionTexts.join(', ')}`;

    modalSelectionDiv.innerText = finalText;
    pageSelectionDiv.innerText = finalText;
  }

  function fetchFilteredCards() {
    const type = window.location.pathname.split("/")[2];
    const query = new URLSearchParams();
    query.append("type", type);
    query.append("page", 0);
    selectedRegions.forEach(region => {
      if (region.id) query.append("regionId", region.id);
    });

    fetch(`/api/reservation?${query.toString()}`)
      .then(response => response.json())
      .then(cards => updateCards(cards))
      .catch(err => {
        console.error("필터링 데이터 요청 실패:", err);
      });
  }

  function updateCards(cards) {
    const container = document.querySelector("#cardContainer");
    container.innerHTML = "";

    cards.forEach(card => {
      const div = document.createElement("div");
      div.className = "ad-card";

      let imgSrc = card.imageUrl;
      if (!imgSrc.startsWith("/images/")) {
        imgSrc = "/images/boat.jpg";
      }

      div.innerHTML = `
        <div class="ad-image">
          <img src="${imgSrc}" alt="예약 이미지" style="width:100%; height:180px; object-fit:cover;">
        </div>
        <div class="ad-desc">${card.title}</div>
        <div class="ad-detail">
          <p>지역: ${card.region ?? "없음"}</p>
          <p>회사명: ${card.companyName ?? "알 수 없음"}</p>
          <p>어종: ${card.fishTypes?.join(", ") ?? "정보 없음"}</p>
          <p>${card.content}</p>
        </div>
      `;
      container.appendChild(div);
    });
  }
});