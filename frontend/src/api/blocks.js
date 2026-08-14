import { request } from './client'

export function getCandidateBlocks(planId) {
    return request(`/blocks?excludePlanId=${planId}`)
}