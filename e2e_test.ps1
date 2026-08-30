$baseUrl = "http://localhost:8080/FixFlow/api"
$headers = @{"Content-Type" = "application/json"}

function Test-Api {
    param($Name, $Method, $Endpoint, $Body, $Token, $ExpectedStatus)
    
    $reqHeaders = $headers.Clone()
    if ($Token) { $reqHeaders["Authorization"] = "Bearer $Token" }
    
    try {
        if ($Body) {
            $res = Invoke-RestMethod -Uri "$baseUrl$Endpoint" -Method $Method -Headers $reqHeaders -Body $Body
        } else {
            $res = Invoke-RestMethod -Uri "$baseUrl$Endpoint" -Method $Method -Headers $reqHeaders
        }
        Write-Host "[PASS] $Name (200/201)" -ForegroundColor Green
        return $res
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "[PASS] $Name (Status: $statusCode)" -ForegroundColor Green
        } else {
            Write-Host "[FAIL] $Name - Expected $ExpectedStatus but got $statusCode" -ForegroundColor Red
        }
        return $null
    }
}

Write-Host "--- AUTHENTICATION TESTS ---"
$loginAdmin = Test-Api -Name "Admin Login" -Method POST -Endpoint "/auth/login" -Body '{"email":"admin@fixflow.local","password":"Password123"}' -ExpectedStatus 200
$adminToken = $loginAdmin.data.token

# 5. TEST: Technician Login
$loginTech = Test-Api -Name "Tech Login" -Method POST -Endpoint "/auth/login" -Body '{"email":"technician@fixflow.local","password":"Password123"}' -ExpectedStatus 200
$techToken = $loginTech.data.token

# 6. TEST: User Login
$loginUser = Test-Api -Name "User Login" -Method POST -Endpoint "/auth/login" -Body '{"email":"user@fixflow.local","password":"Password123"}' -ExpectedStatus 200
$userToken = $loginUser.data.token

Test-Api -Name "Invalid Login" -Method POST -Endpoint "/auth/login" -Body '{"email":"admin@fixflow.local","password":"wrong"}' -ExpectedStatus 401

Write-Host "--- AUTHORIZATION TESTS ---"
Test-Api -Name "Unauthorized Access (No Token)" -Method GET -Endpoint "/users" -ExpectedStatus 401
Test-Api -Name "Forbidden Access (User -> /users)" -Method GET -Endpoint "/users" -Token $userToken -ExpectedStatus 403
Test-Api -Name "Forbidden Access (Tech -> /users)" -Method GET -Endpoint "/users" -Token $techToken -ExpectedStatus 403
Test-Api -Name "Allowed Access (Admin -> /users)" -Method GET -Endpoint "/users" -Token $adminToken -ExpectedStatus 200

Write-Host "--- WORKFLOW TESTS ---"
$newReq = Test-Api -Name "User Creates Request" -Method POST -Endpoint "/requests" -Body '{"title":"Leaky Pipe","description":"Water leaking in dorm A","priority":"HIGH","location":"Dorm A","categoryId":1}' -Token $userToken -ExpectedStatus 201
$reqId = $newReq.data.id

Test-Api -Name "Tech cannot assign" -Method POST -Endpoint "/requests/$reqId/assignment" -Body '{"technicianId":2,"notes":"Get on it"}' -Token $techToken -ExpectedStatus 403

Test-Api -Name "Admin assigns Tech" -Method POST -Endpoint "/requests/$reqId/assignment" -Body '{"technicianId":2,"notes":"Please fix"}' -Token $adminToken -ExpectedStatus 201

Test-Api -Name "Tech starts work" -Method PATCH -Endpoint "/requests/$reqId/status" -Body '{"status":"IN_PROGRESS"}' -Token $techToken -ExpectedStatus 200

Test-Api -Name "Invalid Transition (Tech -> CLOSED)" -Method PATCH -Endpoint "/requests/$reqId/status" -Body '{"status":"CLOSED"}' -Token $techToken -ExpectedStatus 400

Test-Api -Name "Tech resolves" -Method PATCH -Endpoint "/requests/$reqId/status" -Body '{"status":"RESOLVED"}' -Token $techToken -ExpectedStatus 200

Test-Api -Name "Admin closes" -Method PATCH -Endpoint "/requests/$reqId/status" -Body '{"status":"CLOSED"}' -Token $adminToken -ExpectedStatus 200

Write-Host "--- SEARCH / FILTER / PAGINATION TESTS ---"
Test-Api -Name "Get all requests paginated" -Method GET -Endpoint "/requests?page=1&limit=5" -Token $adminToken -ExpectedStatus 200
Test-Api -Name "Filter by status" -Method GET -Endpoint "/requests?status=CLOSED" -Token $adminToken -ExpectedStatus 200
