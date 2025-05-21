package com.test.foodtrip.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleResponse {

    private String sub;                  // 사용자 고유 식별자
    private String name;
    private String given_name;
    private String family_name;
    private String picture;
    private String email;
    private Boolean verified_email;
    private String locale;

    @JsonProperty("sub")
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

}
