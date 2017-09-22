/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paymentez.example;

import com.paymentez.example.model.Customer;
import com.paymentez.example.sdk.Paymentez;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@SpringBootApplication
public class Main {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @RequestMapping("/")
    String index() {
        return "Great, your backend is set up. Now you can configure the Paymentez example apps to point here.";
    }

    /**
     * This code simulates "loading the customer for your current session".
     * Your own logic will likely look very different.
     *
     * @return customer for your current session
     */
    Customer getAuthenticatedCustomer(String uid, HttpServletRequest request){
        Customer customer = new Customer(uid,
                "dev@paymentez.com",
                request.getRemoteAddr());
        return customer;
    }

    /**
     * This endpoint receives an uid and gives you all their cards assigned to that user.
     * Your own logic shouldn't Call Paymentez on every request, instead, you should cache the cards on your own servers.
     *
     * @param uid Customer identifier. This is the identifier you use inside your application; you will receive it in notifications.
     *
     * @return a json with all the customer cards
     */
    @RequestMapping(value = "/get-cards", method = RequestMethod.GET, produces = "application/json")
    String getCards(@RequestParam(value = "uid") String uid, HttpServletResponse response) {

        Map<String, String> mapResponse = Paymentez.doGetRequest(Paymentez.PAYMENTEZ_DEV_URL + "/v2/transaction/list?uid="+uid);
        response.setStatus(Integer.parseInt(mapResponse.get(Paymentez.RESPONSE_HTTP_CODE)));
        return mapResponse.get(Paymentez.RESPONSE_JSON);
    }

    /**
     * This endpoint is used by Android/ios example app to create a charge.
     *
     * @param uid Customer identifier. This is the identifier you use inside your application; you will receive it in notifications.
     * @param session_id  string used for fraud purposes.
     * @param token Card identifier. This token is unique among all cards.
     * @param amount Amount to debit.
     * @param dev_reference Merchant order reference. You will identify this purchase using this reference.
     * @param description Clear descriptions help Customers to better understand what they’re buying.
     *
     * @return a json with the response
     */
    @RequestMapping(value = "/create-charge", method = RequestMethod.POST, produces = "application/json")
    String createCharge(@RequestParam(value = "uid") String uid,
                        @RequestParam(value = "session_id", required = false) String session_id,
                        @RequestParam(value = "token") String token,
                        @RequestParam(value = "amount") double amount,
                        @RequestParam(value = "dev_reference") String dev_reference,
                        @RequestParam(value = "description") String description,
                        HttpServletRequest request, HttpServletResponse response) {
        Customer customer = getAuthenticatedCustomer(uid, request);

        String jsonPaymentezDebit = Paymentez.paymentezDebitJson(customer, session_id, token, amount, dev_reference, description);

        Map<String, String> mapResponse = Paymentez.doPostRequest(Paymentez.PAYMENTEZ_DEV_URL + "/v2/transaction/debit", jsonPaymentezDebit);
        response.setStatus(Integer.parseInt(mapResponse.get(Paymentez.RESPONSE_HTTP_CODE)));
        return mapResponse.get(Paymentez.RESPONSE_JSON);
    }

    /**
     * This endpoint is used by Android/ios example app to delete a card.
     *
     * @param uid Customer identifier. This is the identifier you use inside your application; you will receive it in notifications.
     * @param token Card identifier. This token is unique among all cards.
     *
     * @return a json with the response
     */
    @RequestMapping(value = "/delete-card", method = RequestMethod.POST, produces = "application/json")
    String deleteCard(@RequestParam(value = "uid") String uid,
                        @RequestParam(value = "token") String token, HttpServletResponse response) {

        String jsonPaymentezDelete = Paymentez.paymentezDeleteJson(uid, token);

        Map<String, String> mapResponse = Paymentez.doPostRequest(Paymentez.PAYMENTEZ_DEV_URL + "/v2/transaction/delete", jsonPaymentezDelete);
        response.setStatus(Integer.parseInt(mapResponse.get(Paymentez.RESPONSE_HTTP_CODE)));
        return mapResponse.get(Paymentez.RESPONSE_JSON);
    }

    /**
     * This endpoint is used by Android/ios example app to verify a card or transaction.
     *
     * @param uid Customer identifier. This is the identifier you use inside your application; you will receive it in notifications.
     * @param transaction_id Transaction identifier. This is code is unique among all transactions.
     * @param type It identifies if the value is authorization code or amount (BY_AMOUNT / BY_AUTH_CODE)
     * @param value The authorization code provided by the financial entity to the buyer or the transaction amount.
     *
     * @return a json with the response
     */
    @RequestMapping(value = "/verify-transaction", method = RequestMethod.POST, produces = "application/json")
    String verifyTransaction(@RequestParam(value = "uid") String uid,
                             @RequestParam(value = "transaction_id") String transaction_id, @RequestParam(value = "type") String type,
                      @RequestParam(value = "value") String value, HttpServletResponse response) {

        String jsonPaymentezVerify = Paymentez.paymentezVerifyJson(uid, transaction_id, type, value);

        Map<String, String> mapResponse = Paymentez.doPostRequest(Paymentez.PAYMENTEZ_DEV_URL + "/v2/transaction/verify", jsonPaymentezVerify);
        response.setStatus(Integer.parseInt(mapResponse.get(Paymentez.RESPONSE_HTTP_CODE)));
        return mapResponse.get(Paymentez.RESPONSE_JSON);
    }

}
