const ACTIVITY_TYPE_ICONS = {
    観光: '🗼',
    食事: '🍴',
    宿泊: '🏨',
    買い物: '🛍️',
    体験: '🎨',
    その他: '',
}

const TRANSFER_METHOD_ICONS = {
    徒歩: '🚶',
    自転車: '🚲',
    バス: '🚌',
    電車: '🚃',
    飛行機: '✈️',
    船: '⛴️',
    その他: '',
}

export function getBlockLeadingIcon(block) {
    if (block.blockType === 'activity') {
        return ACTIVITY_TYPE_ICONS[block.activityType] ?? ''
    }

    if (block.blockType === 'transfer') {
        return TRANSFER_METHOD_ICONS[block.transferMethod] ?? ''
    }

    return ''
}

export function getBlockLocationLabel(block) {
    if (block.blockType === 'transfer') {
        const departure = block.transferDeparture || '未設定'
        const arrival = block.transferArrival || '未設定'

        return `${departure} → ${arrival}`
    }

    return block.blockPlace || ''
}