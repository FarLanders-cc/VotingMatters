package cc.farlanders.votingmatters.api.impl;

import java.util.ArrayList;
import java.util.List;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.VoteRecord;

public class PlanetMinecraftAPI extends BaseVoteAPI {

    public PlanetMinecraftAPI(VotingMatters plugin) {
        super(plugin, "planetminecraft");
    }

    @Override
    protected List<VoteRecord> performVoteCheck() {
        return new ArrayList<>();
    }
}
