package com.aloy.coreapp.context;

import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    public UserContext(UserContext source) {
        userId = source.getUserId();
        token = source.getToken();
        email = source.getEmail();
        name = source.getName();
        phoneNumber = source.getPhoneNumber();
        wsToken = source.getWsToken();

        if (!CollectionUtils.isEmpty(source.getAccessRoleSet())) {
            accessRoleSet.addAll(source.getAccessRoleSet());
        }

        if (!MapUtils.isEmpty(source.getAttributes())) {
            attributes.putAll(source.getAttributes());
        }
    }

    private static ThreadLocal<UserContext> context = new ThreadLocal<UserContext>();;
    private String userId;
    private String token;
    private String wsToken;
    private String email;
    private String name;
    private String phoneNumber;
    private Set<String> accessRoleSet = new HashSet<>();
    private Map<String, String> attributes = new HashMap<>();


    public static UserContext current() {
        if (context.get() != null) {
            return context.get();
        }
        else {
            synchronized (UserContext.class) {
                if (context.get() != null) {
                    return context.get();
                }
                else {
                    UserContext uc = new UserContext();
                    context.set(uc);
                    return context.get();
                }
            }
        }
    }

    public static void clear() {
        if (context != null)
            context.remove();
    }
}
