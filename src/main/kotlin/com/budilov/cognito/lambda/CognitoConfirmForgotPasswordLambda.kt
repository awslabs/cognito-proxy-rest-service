package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoConfirmForgotPasswordLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {

    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse {

        val username = request?.headers?.get("username")
        val password = request?.headers?.get("password")

        val confirmationCode = request?.headers?.get("confirmationCode")

        var status = 400
        var response = ""

        if (username != null && password != null && confirmationCode != null) {
            response = Gson().toJson(cognito.confirmForgotPassword(username = username, confirmationCode = confirmationCode, password = password))
            status = 200
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}