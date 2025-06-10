// FishingIndexResult.java
package com.fishtripplanner.api.khoa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FishingIndexResult<T> {

    @JsonProperty("data")
    private List<T> data;
}
