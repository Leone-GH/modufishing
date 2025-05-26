 let routeData = null;
  let selectedVehicle = null;
  let departureValue = null;
  let destinationLat = null;
  let destinationLng = null;

  window.onload = function () {
    kakao.maps.load(() => {
      const map = new kakao.maps.Map(document.getElementById('map'), {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 5
      });

      const geocoder = new kakao.maps.services.Geocoder();
      const places = new kakao.maps.services.Places();
      let waypoints = [];
      let departure = null;
      let destination = null;
      let polyline = null;
      let tempMarker = null;

      const searchBox = document.getElementById('addressSearch');
      const suggestions = document.getElementById('searchSuggestions');
      let debounceTimer;
      searchBox.addEventListener('input', () => {
        const keyword = searchBox.value.trim();
        clearTimeout(debounceTimer);
        if (keyword.length < 2) {
          suggestions.style.display = 'none';
          return;
        }
        debounceTimer = setTimeout(() => {
          places.keywordSearch(keyword, (data, status) => {
            if (status === kakao.maps.services.Status.OK) {
              suggestions.innerHTML = '';
              data.forEach(place => {
                const div = document.createElement('div');
                div.className = 'suggestion';
                div.textContent = place.place_name;
                div.onclick = () => {
                  map.setCenter(new kakao.maps.LatLng(place.y, place.x));
                  suggestions.style.display = 'none';
                };
                suggestions.appendChild(div);
              });
              suggestions.style.display = 'block';
            }
          });
        }, 300);
      });

      window.openSearchModal = () => {
        const keyword = searchBox.value.trim();
        if (keyword.length < 2) return;
        places.keywordSearch(keyword, (data, status) => {
          if (status === kakao.maps.services.Status.OK) {
            const modal = document.getElementById('searchModal');
            modal.innerHTML = '<h3>검색 결과</h3>';
            data.forEach(place => {
              const div = document.createElement('div');
              div.textContent = place.place_name;
              div.style.cursor = 'pointer';
              div.onclick = () => {
                map.setCenter(new kakao.maps.LatLng(place.y, place.x));
                modal.style.display = 'none';
              };
              modal.appendChild(div);
            });
            modal.style.display = 'block';
          }
        });
      };

      function tryAutoFetchRoute() {
        if (departure && destination) {
          fetch(`/api/route?startX=${departure.getPosition().getLng()}&startY=${departure.getPosition().getLat()}&endX=${destination.getPosition().getLng()}&endY=${destination.getPosition().getLat()}${waypoints.length ? '&waypoints=' + waypoints.map(m => m.getPosition().getLng() + ',' + m.getPosition().getLat()).join('_') : ''}`)
            .then(res => res.json())
            .then(data => {
             routeData = data;
              if (polyline) polyline.setMap(null);
              const route = data.routes[0];
              const path = route.path.map(p => new kakao.maps.LatLng(p[1], p[0]));
              polyline = new kakao.maps.Polyline({
                path,
                strokeWeight: 5,
                strokeColor: '#007BFF',
                strokeOpacity: 0.9,
                strokeStyle: 'solid'
              });
              polyline.setMap(map);


              ///////////////////////////////////////////////////////////////////////////
  // 예상 시간, 거리, 비용 표시
  const minutes = Math.round(data.duration / 60);
  const toll = data.toll || 0;

  departureValue = document.getElementById('departure-date').value; // "2025-05-21T03:30"
  const departureTime = new Date(departureValue); // JS Date 객체
  const durationMinutes = Math.round(data.duration / 60);

  const arrivalTime = new Date(departureTime.getTime() + durationMinutes * 60000); // 도착시각

  document.getElementById('durationText').textContent = `예상 소요 시간: 약 ${minutes}분`;


  document.getElementById('tollText').textContent = `예상 톨게이트 비용: 약 ${toll.toLocaleString()}원`;

  document.getElementById('routeInfo').style.display = 'block';

  callMarineInfoAfterRouteCalc();




   // ✅ 연료비 계산 추가
        if (selectedVehicle) {
          calculateFuelCost(routeData, selectedVehicle).then(result => {
            if (result) {
              document.getElementById("fuelText").innerHTML =
                `🚘 선택 차량: ${carInput.value}<br>` +
                `도심연비: ${selectedVehicle.cityEff} km/L, 고속도로연비: ${selectedVehicle.highwayEff} km/L<br>` +
                `연료 종류: ${selectedVehicle.fuelType}<br><br>` +
                `예상 연료 사용량: ${result.fuelUsed}L<br>` +
                `유류 단가 (${result.fuelType}): ${result.fuelPrice.toLocaleString()}원/L<br>` +
                `<strong>예상 연료비: ${result.fuelCost.toLocaleString()}원</strong>`;
            }
          });
        }
});

        }

      }


      kakao.maps.event.addListener(map, 'rightclick', function(mouseEvent) {
        const latlng = mouseEvent.latLng;
        const containerRect = document.getElementById('map').getBoundingClientRect();
        const menuX = containerRect.left + mouseEvent.point.x;
        const menuY = containerRect.top + mouseEvent.point.y;

        if (tempMarker) tempMarker.setMap(null);
        tempMarker = new kakao.maps.Marker({ position: latlng, map });

        const menu = document.getElementById('mapContextMenu');
        menu.innerHTML = `
          <button onclick="selectPoint('departure')">출발지</button>
          <button onclick="selectPoint('waypoint')">경유지</button>
          <button onclick="selectPoint('destination')">도착지</button>
        `;
        menu.style.left = `${menuX}px`;
        menu.style.top = `${menuY}px`;
        menu.style.display = 'block';

        window.selectPoint = function(type) {
  const position = tempMarker.getPosition();
  tempMarker.setMap(null);
  tempMarker = null;
  menu.style.display = 'none';

  if (type === 'departure') {
    if (departure) departure.setMap(null);
    departure = new kakao.maps.Marker({ position, map });
    geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
      if (status === kakao.maps.services.Status.OK) {
        document.getElementById('departurePoint').value = result[0].address.address_name;
        tryAutoFetchRoute();
      }
    });
  } else if (type === 'destination') {
    if (destination) destination.setMap(null);
    destination = new kakao.maps.Marker({ position, map });
    destinationLat = position.getLat(); // ✅ 추가
    destinationLng = position.getLng(); // ✅ 추가
    geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
      if (status === kakao.maps.services.Status.OK) {
        document.getElementById('destination').value = result[0].address.address_name;
        tryAutoFetchRoute();
      }
    });
  }
   else if (type === 'waypoint') {
    const marker = new kakao.maps.Marker({ position, map });
     geocoder.coord2Address(position.getLng(), position.getLat(), (result, status) => {
          if (status === kakao.maps.services.Status.OK) {
            document.getElementById('waypoint').value = result[0].address.address_name;}});
    waypoints.push(marker);

    kakao.maps.event.addListener(marker, 'click', () => {
      if (confirm('이 마커를 삭제할까요?')) {
        marker.setMap(null);
        waypoints = waypoints.filter(m => m !== marker);
        tryAutoFetchRoute();
      }
    });
    tryAutoFetchRoute();
  }
};
      });

      document.getElementById('partyForm').addEventListener('submit', () => {
        const container = document.getElementById('waypointInputs');
        container.innerHTML = '';
        waypoints.forEach((marker, idx) => {
          const lat = marker.getPosition().getLat();
          const lng = marker.getPosition().getLng();
          container.innerHTML += `
            <input type="hidden" name="waypoints[${idx}].lat" value="${lat}" />
            <input type="hidden" name="waypoints[${idx}].lng" value="${lng}" />
            <input type="hidden" name="waypoints[${idx}].name" value="경유지${idx + 1}" />
          `;
        });
        // 🔽 출발지/도착지 좌표 hidden input 추가
          if (departure) {
            container.innerHTML += `
              <input type="hidden" name="departureLat" value="${departure.getPosition().getLat()}" />
              <input type="hidden" name="departureLng" value="${departure.getPosition().getLng()}" />
            `;
          }
          if (destination) {
            container.innerHTML += `
              <input type="hidden" name="destinationLat" value="${destination.getPosition().getLat()}" />
              <input type="hidden" name="destinationLng" value="${destination.getPosition().getLng()}" />
            `;
          }
      });
    });
  }



  ////////////////////////////////////////////////////////////////////////////////////////////////
  // 연료 단가 비동기로 받아서 유류비 계산에 반영
async function calculateFuelCost(routeData, vehicleData) {
  const distanceKm = routeData.distance / 1000;
  const cityEff = vehicleData.cityEff;
  const highwayEff = vehicleData.highwayEff;
  const fuelType = vehicleData.fuelType;

  // 최신 유가 정보 fetch
  const fuelPrices = await fetch("/api/fuel-price")
    .then(res => res.json())
    .catch(() => {
      alert("유가 정보를 불러올 수 없습니다.");
      return null;
    });

  if (!fuelPrices || !fuelPrices[fuelType]) {
    alert(`'${fuelType}'에 대한 유가 정보가 없습니다.`);
    return;
  }

  const fuelPrice = fuelPrices[fuelType]; // 최신 단가 적용

  // 가중 평균 연비
  const weightedEff = routeData.highwayRatio * highwayEff + routeData.generalRatio * cityEff;
  const usedFuel = distanceKm / weightedEff;
  const fuelCost = Math.round(usedFuel * fuelPrice);

  // 결과 객체 반환
  return {
    fuelUsed: usedFuel.toFixed(2),
    fuelCost: fuelCost,
    fuelType: fuelType,
    fuelPrice: fuelPrice
  };
}
  /////////////////////////////////////////////////////////////
  //차량 자동검색 및 연비 출력
 const carInput = document.getElementById("carSearchInput");
const suggestionBox = document.getElementById("carSuggestions");


let carDebounce;
carInput.addEventListener("input", () => {
  const keyword = carInput.value.trim();
  clearTimeout(carDebounce);
  if (keyword.length < 2) {
    suggestionBox.style.display = "none";
    return;
  }

  carDebounce = setTimeout(() => {
    fetch(`/api/car/search?keyword=${encodeURIComponent(keyword)}`)
      .then(res => res.json())
      .then(data => {
        suggestionBox.innerHTML = "";
        data.forEach(fullModelName => {
          const div = document.createElement("div");
          div.textContent = fullModelName; // 예: "볼보 XC60"
          div.classList.add("suggestion");
          div.style.padding = "8px";
          div.style.cursor = "pointer";
          div.onclick = () => selectCarModel(fullModelName);
          suggestionBox.appendChild(div);
        });
        suggestionBox.style.display = "block";
      });
  }, 300);
});

function selectCarModel(modelName) {
  console.log("선택된 모델명:", modelName);
  carInput.value = modelName;
  suggestionBox.style.display = "none";

  fetch(`/api/car/model?name=${encodeURIComponent(modelName)}`)
    .then(res => {
      if (!res.ok) throw new Error("차량 정보 없음");
      return res.json();
    })
    .then(data => {
      console.log("차량 정보 받아옴:", data);
      selectedVehicle = {
        cityEff: data.cityEff,
        highwayEff: data.highwayEff,
        fuelType: data.fuelType
      };

      document.getElementById("fuelText").innerHTML =
        `🚘 선택 차량: ${modelName}<br>` +
        `도심연비: ${data.cityEff} km/L, 고속도로연비: ${data.highwayEff} km/L<br>` +
        `연료 종류: ${data.fuelType}`;

      if (routeData) {
        console.log("routeData 있음. 연료비 계산 시작");
        calculateFuelCost(routeData, selectedVehicle).then(result => {
          if (result) {
            document.getElementById("fuelText").innerHTML +=
              `<br><br>예상 연료 사용량: ${result.fuelUsed}L<br>` +
              `유류 단가 (${result.fuelType}): ${result.fuelPrice.toLocaleString()}원/L<br>` +
              `<strong>예상 연료비: ${result.fuelCost.toLocaleString()}원</strong>`;
          }
        }).catch(err => {
          console.error("연료비 계산 에러:", err);
        });
      } else {
        console.warn("routeData가 아직 null임. 지도에서 출발지/도착지를 먼저 지정해야 함.");
      }
    })
    .catch((err) => {
      console.error("🚨 차량 정보 로드 실패", err);
      alert("차량 정보 로드 실패");
    });
}
function renderMarineInfo(data, areaName) {
  const section = document.getElementById("weatherSection");
  section.innerHTML = "";

  console.log("🔍 도착지:", destinationLat, destinationLng, areaName);

  // ▶️ 추천 낚시 시간
  if (data.recommendedTime) {
    const { date, time, fishType, waterTemp, currentSpeed, waveHeight, airTemp, tide, fishingIndex, fishingScore } = data.recommendedTime;

    const box = document.createElement("div");
    box.innerHTML = `
      <h3>🎯 추천 낚시 시간</h3>
      <p><strong>${date} ${time}</strong></p>
      <ul>
        <li>🎣 어종: <strong>${fishType}</strong></li>
        <li>🌡️ 수온: ${waterTemp}℃</li>
        <li>🌬️ 풍속: ${currentSpeed} m/s</li>
        <li>🌊 파고: ${waveHeight} m</li>
        <li>🌤️ 기온: ${airTemp}℃</li>
        <li>🌀 조류: ${tide}</li>
        <li>📊 낚시 지수: <strong>${fishingIndex}</strong> (${fishingScore}점)</li>
      </ul>
    `;
    applyBoxStyle(box, "#007BFF", "#f0f8ff");
    section.appendChild(box);
  }

  // ▶️ 실시간 관측소 정보
  if (data.observation && Object.keys(data.observation).length > 0) {
    const obsWrapper = document.createElement("div");
    obsWrapper.innerHTML = "<h3>📍 관측소 정보</h3>";

    for (const [key, value] of Object.entries(data.observation)) {
      const obsBox = document.createElement("div");
      obsBox.innerHTML = `
        <strong>${key}</strong> (${value.stationName})<br/>
        위치: (${value.lat.toFixed(4)}, ${value.lon.toFixed(4)})<br/>
        유형: ${value.dataType}
      `;
      applyDashedBoxStyle(obsBox);
      obsWrapper.appendChild(obsBox);
    }

    section.appendChild(obsWrapper);
  }

  // ▶️ 조석 예보
  if (Array.isArray(data.tideForecast) && data.tideForecast.length > 0) {
    const tideBox = document.createElement("div");
    tideBox.innerHTML = "<h3>🌊 조석 예보</h3>";

    const table = document.createElement("table");
    table.style.width = "100%";
    table.style.borderCollapse = "collapse";

    const headerRow = `
      <thead>
        <tr style="background: #eee;">
          <th style="border: 1px solid #ccc; padding: 5px;">시간</th>
          <th style="border: 1px solid #ccc; padding: 5px;">구분</th>
          <th style="border: 1px solid #ccc; padding: 5px;">조위(cm)</th>
        </tr>
      </thead>
    `;

    const bodyRows = data.tideForecast.map(t => `
      <tr>
        <td style="border: 1px solid #ccc; padding: 5px;">${t.recordTime}</td>
        <td style="border: 1px solid #ccc; padding: 5px;">${t.tideCode}</td>
        <td style="border: 1px solid #ccc; padding: 5px;">${t.tideLevel}</td>
      </tr>
    `).join("");

    table.innerHTML = headerRow + `<tbody>${bodyRows}</tbody>`;

    tideBox.appendChild(table);
    tideBox.style.marginTop = "20px";
    tideBox.style.border = "1px solid #ccc";
    tideBox.style.padding = "10px";
    tideBox.style.borderRadius = "8px";

    section.appendChild(tideBox);
  }
}
//여기가 바뀌는거예요
function callMarineInfoAfterRouteCalc() {
  const departureInput = document.getElementById("departure-date");
  const destinationInput = document.getElementById("destination");

  if (!departureInput || !destinationInput) {
    console.warn("출발일시 또는 도착지 입력 필드를 찾을 수 없습니다.");
    return;
  }

  const departureValue = departureInput.value;
  const destinationName = destinationInput.value;

  if (!departureValue || !destinationName || !destinationLat || !destinationLng) {
    console.warn("해양정보 호출 조건 미충족: 출발일시, 도착지 이름 또는 좌표 누락");
    return;
  }

  const durationText = document.getElementById("durationText")?.textContent;
  const match = durationText?.match(/약\s(\d+)분/);
  if (!match) {
    console.warn("소요 시간 정보가 부족하거나 형식이 일치하지 않습니다.");
    return;
  }

  const durationMinutes = parseInt(match[1], 10);
  const departureTime = new Date(departureValue);
  const arrival = new Date(departureTime.getTime() + durationMinutes * 60000);
  const dateStr = departureTime.toISOString().split("T")[0]; // YYYY-MM-DD
  const arrivalStr = arrival.toTimeString().slice(0, 5);      // HH:mm

  const params = new URLSearchParams({
    lat: destinationLat,
    lon: destinationLng,
    area: destinationName,
    date: dateStr,
    arrivalTime: arrivalStr
  });

  fetch(`/api/marine-info?${params.toString()}`)
    .then(res => {
      if (!res.ok) throw new Error("해양 API 응답 오류");
      return res.json();
    })
    .then(data => {
      console.log("📡 해양 API 결과", data);
      renderMarineInfo(data, destinationName);
    })
    .catch(err => {
      console.error("🌊 해양 API 호출 실패", err);
      alert("해양 정보를 불러오지 못했습니다. 네트워크 상태를 확인하세요.");
    });
}
function applyBoxStyle(el, borderColor, bgColor) {
  el.style.border = `2px solid ${borderColor}`;
  el.style.background = bgColor;
  el.style.borderRadius = "10px";
  el.style.margin = "10px";
  el.style.padding = "15px";
}

function applyDashedBoxStyle(el) {
  el.style.border = "1px dashed #999";
  el.style.margin = "5px 0";
  el.style.padding = "10px";
  el.style.borderRadius = "6px";
}
