package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoConfirmSignupLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {

    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse {
        val logger = context?.logger

        val username = request?.headers?.get("username")
        val confirmationCode = request?.headers?.get("confirmationCode")

        var status = 400
        var response = ""

        if (username != null && confirmationCode != null) {
            status = 200
            response = Gson().toJson(cognito.confirmSignUp(username = username, confirmationCode = confirmationCode))
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}