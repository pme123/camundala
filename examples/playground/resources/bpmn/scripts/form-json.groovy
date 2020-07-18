existingAddr = execution.getVariableTyped('existingAddress').getValue()
clientKey = execution.getVariable('clientKey')

formJson = """{
        "existingAddress": $existingAddr,
        "clientKey": "$clientKey"
    }"""
asJson(formJson)

