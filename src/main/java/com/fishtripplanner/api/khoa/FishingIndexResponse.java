// FishingIndexResponse.java
package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FishingIndexResponse<T> {

    @JsonProperty("result")
    private FishingIndexResult<T> result;

}
