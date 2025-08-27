package com.mjanglin.votingmatters.api.impl;

import java.util.ArrayList;
import java.util.List;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.models.VoteRecord;

public class MinecraftMPAPI extends BaseVoteAPI {

    public MinecraftMPAPI(VotingMatters plugin) {
        super(plugin, "minecraftmp");
    }

    @Override
    protected List<VoteRecord> performVoteCheck() {
        return new ArrayList<>();
    }
}
