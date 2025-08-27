package com.mjanglin.votingmatters.api.impl;

import java.util.ArrayList;
import java.util.List;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.models.VoteRecord;

public class TopMinecraftServersAPI extends BaseVoteAPI {

    public TopMinecraftServersAPI(VotingMatters plugin) {
        super(plugin, "topminecraftservers");
    }

    @Override
    protected List<VoteRecord> performVoteCheck() {
        return new ArrayList<>();
    }
}
