NorthstarCuriosEvents.equipmentCheck(event => {
    if (event.stack.id !== 'minecraft:golden_helmet') {
        return
    }

    switch (event.type) {
        case 'oxygen_source':
            event.grant(4000)
            break
        case 'full_seal':
            event.grant(1)
            break
        case 'insulation':
        case 'heat_resistance':
            event.grant(4)
            break
    }
})
