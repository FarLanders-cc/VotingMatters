package com.mjanglin.votingmatters.api.impl;

import java.util.ArrayList;
import java.util.List;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.models.VoteRecord;

public class PlanetMinecraftAPI extends BaseVoteAPI {

    public PlanetMinecraftAPI(VotingMatters plugin) {
        super(plugin, "planetminecraft");
    }

    @Override
    protected List<VoteRecord> performVoteCheck() {
        return new ArrayList<>();
    }
}
