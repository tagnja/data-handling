package com.taogen.app.export;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Taogen
 */
@Data
@AllArgsConstructor
public class InsertUserInfo {
    private String customerId;
    private String customerName;
    private String userName;
    private String password;
}
