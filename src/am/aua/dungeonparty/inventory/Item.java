package am.aua.dungeonparty.inventory;

import am.aua.dungeonparty.core.TurnResult;

public interface Item {
    TurnResult use(am.aua.dungeonparty.core.Player user,
                   am.aua.dungeonparty.core.Player target);

    String getName();

    String getDescription();
}