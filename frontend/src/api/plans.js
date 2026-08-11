improt { request } from './client'

export function getPlans() {
    return request('/plans')
}

export function getPlan(planId) {
    return request(`/plans/${planId}`)
}

export function createPlans(payload) {
    return request('/plans', {
        method: 'POST',
        body: JSON.stringify(payload),
    })
}

export function updatePlan(planId, payload) {
    return request(`/plans/${planId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    })
}

export function duplicatePlan(planId) {
    return request(`/plans/${planId}/duplicate`, {
        method: 'POST',
    })
}

export function deletePlan(planId) {
    return request(`/plans/${planId}`, {
        method: 'DELETE',
    })
}

export function updatePositions(planId, positions) {
    return request(`/plans/${planId}/positions`, {
        method: 'PUT',
        body: JSON.stringify({ positions }),
    })
}

export function removeBlockFromPlan(planId, blockId) {
    return request(`/plans/${planId}/blocks/${blockId}`, {
        method: 'DELETE',
    })
}

export function getPlanTodos(planId) {
    return request(`/plans/${planId}/todos`)
}
