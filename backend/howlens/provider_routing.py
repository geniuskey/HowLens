"""Explicit stage policies. Enable only in a reviewed deployment configuration."""
from dataclasses import dataclass

REASONING_MODELS = {'gpt-6-luna', 'gpt-6.1-sol', 'gpt-6-astra'}


@dataclass(frozen=True)
class StagePolicy:
    model: str
    effort: str
    timeout_seconds: float
    max_output_tokens: int

    def __post_init__(self):
        allowed = {'low', 'medium', 'high', 'xhigh', 'max'}
        if self.model == 'gpt-6-luna':
            allowed.add('none')
        if (self.model not in REASONING_MODELS or self.effort not in allowed
                or not 0 < self.timeout_seconds <= 25
                or not 1 <= self.max_output_tokens <= 8192):
            raise ValueError('Invalid stage policy')


def configured_stage_policies(env):
    if env.get('HOWLENS_ROLE_ROUTING_ENABLED') != 'true':
        return {}
    triage = env.get('HOWLENS_TRIAGE_MODEL') or 'gpt-6-luna'
    analysis = env.get('HOWLENS_ANALYSIS_MODEL') or 'gpt-6.1-sol'
    research = env.get('HOWLENS_RESEARCH_MODEL') or 'gpt-6.1-sol'
    escalation = env.get('HOWLENS_ESCALATION_MODEL') or 'gpt-6-astra'
    triage_effort = env.get('HOWLENS_TRIAGE_REASONING_EFFORT') or 'low'
    # Individual caps never extend the aggregate deadline. These configured
    # budgets are not measured live latency guarantees.
    research_effort = env.get('HOWLENS_RESEARCH_REASONING_EFFORT') or 'medium'
    analysis_effort = env.get('HOWLENS_ANALYSIS_REASONING_EFFORT') or 'medium'
    escalation_effort = env.get('HOWLENS_ESCALATION_REASONING_EFFORT') or 'low'
    def seconds(role, default):
        return float(env.get(f'HOWLENS_{role}_TIMEOUT_SECONDS') or default)
    return {
        'analysis': StagePolicy(analysis, analysis_effort, seconds('INITIAL', 20), 2500),
        'label': StagePolicy(triage, triage_effort, seconds('TRIAGE', 8), 1500),
        'research': StagePolicy(research, research_effort, seconds('RESEARCH', 20), 2500),
        'reanalysis': StagePolicy(escalation, escalation_effort, seconds('ESCALATION', 15), 3000),
        'verification': StagePolicy(research, research_effort, 20, 2500),
    }


def configured_analysis_timeout(env):
    value = float(env.get('HOWLENS_ANALYSIS_TIMEOUT_SECONDS')
                  or ('40' if env.get('HOWLENS_ROLE_ROUTING_ENABLED') == 'true' else '30'))
    if not 4 <= value <= 40:
        raise ValueError('Analysis timeout must fit the current client read deadline')
    return value
