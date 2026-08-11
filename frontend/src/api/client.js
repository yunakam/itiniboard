const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

export class ApiError extends Error {
    // エラー専用設計図
    constructor(message, status, details = null) {
        super(message)
        this.name = 'ApiError'
        this.status = status
        this.details = details
        }
    }

export async function request(path, options = {}) {

    const { headers: customHeaders, body, ...fetchOptions } = options
    const headers = new Headers(customHeaders)

    if (!headers.has('Accept')) {
        headers.set('Accept', 'application/json')
    }

    if (body !== undefined && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json')
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...fetchOptions,
        body,
        headers,
    })

    if (response.status === 204) {
        return null
        }

    const contentType = response.headers.get('content-type') ?? ''
    const responseBody = contentType.includes('application/json')
        ? await response.json()
        : await response.text()

    if (!response.ok) {
        const message =
            typeof responseBody === 'object' && responseBody?.message
            ? responseBody.message
            : '通信に失敗しました。時間をおいて再度お試しください。'

        throw new ApiError(message, response.status, body)
    }

    return responseBody
}
