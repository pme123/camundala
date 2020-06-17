existingAddr = execution.getVariableTyped('existingAddress').getValue()
customer = execution.getVariable('customer')

formJson = """{
        "existingAddress": $existingAddr,
        "customer": "$customer"
    }"""
asJson(formJson)

