import { request } from './client'

export function getCandidateBlocks(planId) {
    return request(`/blocks?excludePlanId=${planId}`)
}

export function getBlock(blockId) {
    return request(`/blocks/${blockId}`)
}

export function createBlock(payload) {
    return request('/blocks', {
        method: 'POST',
        body: JSON.stringify(payload)
    })
}

export function updateBlock(blockId, payload) {
    return request(`/blocks/${blockId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
  })
}