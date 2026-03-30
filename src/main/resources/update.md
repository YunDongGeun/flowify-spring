# 선택지 매핑 규칙 JSON 스키마 설명서

## 1. 파일 위치 및 용도

**파일:** `./docs/mapping_rules.json`

**용도:** 백엔드 서버가 이 파일을 읽어서, 사용자가 직접 설정할 때 이전 노드의 출력 데이터 타입에 맞는 선택지를 동적으로 제공한다.

**사용 흐름:**

1. 사용자가 "+" 버튼을 눌러 다음 노드를 추가한다
2. 백엔드가 이전 노드의 output_data_type을 확인한다
3. mapping_rules.json에서 해당 데이터 타입의 선택지를 조회한다
4. 프론트엔드에 선택지 목록을 반환한다
5. 사용자가 선택하면, 선택된 항목의 node_type으로 내부 노드가 결정된다

---

## 2. 최상위 구조

```
{
  "_meta": { ... },          // 버전, 설명, 수정일
  "data_types": { ... },     // 핵심: 데이터 타입별 선택지 매핑
  "node_types": { ... },     // 노드 타입 정의 (6가지)
  "service_fields": { ... }  // 서비스별 필드 목록 (데이터 필터용)
}
```

---

## 3. data_types 구조

각 데이터 타입은 다음 구조를 갖는다:

```
"DATA_TYPE_ID": {
  "label": "사용자에게 보여줄 이름",
  "description": "설명",
  "requires_processing_method": true/false,   // 1차 처리 방식 질문 필요 여부
  "processing_method": { ... },               // requires_processing_method가 true일 때만
  "actions": [ ... ]                          // 사용자 선택지 목록
}
```

### requires_processing_method

목록형 데이터(파일 목록, 이메일 목록, 스프레드시트 여러 행)에서 true.
"한 건씩" / "전체 사용"을 먼저 묻는다.
단일 데이터(단일 파일, 단일 이메일, 텍스트, 일정, API 응답)에서 false.

### actions 배열

각 선택지 항목:

```
{
  "id": "고유 식별자",
  "label": "사용자에게 보여줄 텍스트",
  "node_type": "LOOP | CONDITION_BRANCH | AI | DATA_FILTER | AI_FILTER | PASSTHROUGH",
  "output_data_type": "이 선택지를 고르면 나가는 데이터 타입",
  "priority": 1~99,           // 순서 (낮을수록 위에 표시, 추천 순서)
  "applicable_when": { ... }, // 선택적: 특정 조건에서만 보여줄 때
  "follow_up": { ... },       // 선택적: 후속 설정 화면
  "branch_config": { ... }    // 선택적: 조건 분기 전용 설정
}
```

### priority

선택지 순서를 결정한다. 숫자가 낮을수록 위에 표시된다. 추천 항목은 priority를 낮게 설정하여 상단에 노출한다. "그대로 전달"은 항상 priority 99로 맨 아래.

### follow_up

선택 후 보여줄 후속 설정:

```
"follow_up": {
  "question": "어떻게 요약할까요?",
  "options": [
    { "id": "brief", "label": "핵심 3줄 요약" },
    { "id": "custom", "label": "직접 입력", "type": "text_input" }
  ]
}
```

type이 "text_input"이면 사용자가 자유 입력할 수 있는 필드를 표시한다.

### branch_config

조건 분기 노드 전용. 분기 기준 선택:

```
"branch_config": {
  "question": "어떤 파일 종류를 구분할까요?",
  "options": [ ... ],
  "multi_select": true        // 여러 분기 경로를 동시에 선택 가능
}
```

### applicable_when

특정 조건에서만 보여줄 선택지:

```
"applicable_when": { "file_subtype": ["image"] }
```

이미지 파일일 때만 "이미지 내용 설명 생성" 선택지를 표시.

### options_source

선택지가 고정이 아니라 데이터에서 동적으로 가져오는 경우:

- "fields_from_data": 스프레드시트의 컬럼 목록에서 선택지를 생성
- "fields_from_service": service_fields에서 해당 서비스의 필드 목록을 가져옴

---

## 4. node_types

6가지 노드 타입 정의:

| ID | 이름 | 설명 |
| --- | --- | --- |
| LOOP | Loop | 목록형 데이터를 한 건씩 반복 처리 |
| CONDITION_BRANCH | 조건 분기 | 조건에 따라 경로를 분리 |
| AI | AI 처리 | LLM 기반 데이터 가공 |
| DATA_FILTER | 데이터 필터 | 특정 필드/조건으로 데이터 선별 |
| AI_FILTER | AI 필터 | AI가 내용을 판단하여 필터링 |
| PASSTHROUGH | 패스스루 | 변환 없이 그대로 전달 |

---

## 5. service_fields

웹 수집 및 캘린더 등 서비스에서 데이터 필터 시 선택 가능한 필드 목록.
API_RESPONSE 타입에서 "필요한 항목만 선택" 시 이 목록을 참조한다.

새 서비스 추가 시 이 섹션에 필드 목록만 추가하면 된다.

---

## 6. 백엔드 처리 흐름 (의사코드)

```
function getOptionsForNode(previousOutputType, context):
    dataType = mappingRules.data_types[previousOutputType]

    // 1차: 처리 방식 질문이 필요한지 확인
    if dataType.requires_processing_method:
        return dataType.processing_method

    // 2차: 선택지 목록 반환
    options = dataType.actions

    // applicable_when 필터링
    options = options.filter(opt =>
        !opt.applicable_when || matchesContext(opt.applicable_when, context)
    )

    // priority 순서로 정렬
    options.sort(by: priority)

    return options

function onUserSelect(selectedOption):
    // 노드 타입 결정
    nodeType = selectedOption.node_type

    // 후속 설정이 있으면 후속 화면 표시
    if selectedOption.follow_up:
        return selectedOption.follow_up
    if selectedOption.branch_config:
        return selectedOption.branch_config

    // 나가는 데이터 타입으로 다음 노드 선택지 준비
    nextDataType = selectedOption.output_data_type
    return { nodeType, nextDataType }
```

---

## 7. 확장 방법

### 새 서비스 추가

service_fields에 서비스명과 필드 목록을 추가한다.

### 새 선택지 추가

해당 데이터 타입의 actions 배열에 항목을 추가한다. priority로 순서를 조정한다.

### 새 데이터 타입 추가

data_types에 새 키를 추가하고, 선택지와 매핑을 정의한다.

### 선택지 순서 변경 (추천 조정)

해당 항목의 priority 값을 변경한다.

---

## 8. 현재 한계 및 향후 개선

- options_source가 "fields_from_data"인 경우, 런타임에 실제 데이터의 컬럼 목록을 읽어야 하므로 백엔드 로직이 필요하다.
- follow_up이 1단계만 정의되어 있다. 3차, 4차 후속 설정이 필요하면 follow_up 안에 follow_up을 중첩하는 구조로 확장할 수 있다.
- "AI 분류 → 조건 분기" 2단계 패턴은 현재 별도 노드 2개로 처리한다. JSON에서는 각각의 노드 선택에 의해 순차적으로 구성된다.
