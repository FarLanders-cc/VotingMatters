package cc.farlanders.votingmatters.api.impl;

import java.util.ArrayList;
import java.util.List;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.VoteRecord;

public class TopMinecraftServersAPI extends BaseVoteAPI {

    public TopMinecraftServersAPI(VotingMatters plugin) {
        super(plugin, "topminecraftservers");
    }

    @Override
    protected List<VoteRecord> performVoteCheck() {
        return new ArrayList<>();
    }
}
