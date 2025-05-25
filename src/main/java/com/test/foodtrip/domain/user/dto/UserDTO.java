package com.test.foodtrip.domain.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserDTO {

	private String user_id; 
    private String social_type;
    private String social_email;
    private String email;
    private String name;
    private String nickname;
    private String gender;
    private String birth_date;
    private String phone;
    private String address;
    private String greeting;
    private String profile_image;
}
