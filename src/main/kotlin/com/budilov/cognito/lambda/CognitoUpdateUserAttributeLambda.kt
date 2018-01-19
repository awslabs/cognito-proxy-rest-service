package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoUpdateUserAttributeLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {

    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse {
        val logger = context?.logger

        val username = request?.headers?.get("username")
        val attributeName = request?.headers?.get("attributeName")
        val attributeValue = request?.headers?.get("attributeValue")


        var status = 400
        var response = ""

        if (username != null && attributeName != null
                && attributeValue != null) {
            response = Gson().toJson(cognito.updateUserAttribute(username = username,
                    attributeName = attributeName, attributeValue = attributeValue))
            status = 200
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}