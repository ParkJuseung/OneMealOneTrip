package com.test.foodtrip.domain.user.service;

//@Service
//public class CustomOAuth2UserService2
//        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//
//    private final UserRepository userRepository;
//    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public CustomOAuth2UserService2(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest request)
//            throws OAuth2AuthenticationException {
//
//        OAuth2User oauth2User = delegate.loadUser(request);
//
//        String provider = request.getClientRegistration().getRegistrationId();
//
//        Map<String,Object> attrs = oauth2User.getAttributes();
//
//        String socialEmail;
//        String name;
//
//        if ("naver".equals(provider)) {
//            NaverResponse.Response resp =
//                    mapper.convertValue(attrs.get("response"), NaverResponse.Response.class);
//            socialEmail = resp.getEmail();
//            name        = resp.getName();
//
//        } else if ("google".equals(provider)) {
//            GoogleResponse resp = mapper.convertValue(attrs, GoogleResponse.class);
//            socialEmail = resp.getEmail();
//            name        = resp.getName();
//
//        } else {
//            KakaoResponse resp = mapper.convertValue(attrs, KakaoResponse.class);
//            socialEmail = resp.getKakaoAccount().getEmail();
//            name        = resp.getKakaoAccount().getProfile().getNickname();
//        }
//
//        Optional<User> userOpt =
//                userRepository.findBySocialTypeAndSocialEmail(provider, socialEmail);
//
//        HttpSession session = ((ServletRequestAttributes)
//                RequestContextHolder.getRequestAttributes())
//                .getRequest().getSession();
//
//        if (userOpt.isEmpty()) {
//            session.setAttribute("oauth2_attrs", Map.of(
//                    "provider",     provider,
//                    "social_email", socialEmail,
//                    "name",         name
//            ));
//        } else {
//            // 기존 유저면 바로 user_id 세션에 담고
//            session.setAttribute("user_id", userOpt.get().getId());
//        }
//
//        return oauth2User;
//    }
//}

