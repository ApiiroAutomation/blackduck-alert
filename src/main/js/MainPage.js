import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Redirect, Route, withRouter } from 'react-router-dom';
import Navigation from 'Navigation';
import Audit from 'component/audit/Index';
import AboutInfo from 'component/AboutInfo';
import DistributionConfiguration from 'distribution/Index';
import { getDescriptors } from 'store/actions/descriptors';
import SchedulingConfiguration from 'component/SchedulingConfiguration';
import SlackConfiguration from 'channels/SlackConfiguration';
import EmailConfiguration from 'channels/EmailConfiguration';
import HipChatConfiguration from 'channels/HipChatConfiguration';
import LogoutConfirmation from 'component/common/LogoutConfirmation';
import BlackDuckConfiguration from 'providers/BlackDuckConfiguration';
import SettingsConfiguration from 'component/settings/SettingsConfiguration';
import * as DescriptorUtilities from 'util/descriptorUtilities';


class MainPage extends Component {
    constructor(props) {
        super(props);
        this.createRoutesForDescriptors = this.createRoutesForDescriptors.bind(this);
    }

    componentDidMount() {
        this.props.getDescriptors();
    }

    createRoutesForDescriptors(descriptorType, context, uriPrefix) {
        const { descriptors } = this.props;
        if (!descriptors.items) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors.items, descriptorType, context);

        if (!descriptorList || descriptorList.length === 0) {
            return null;
        }
        const routeList = descriptorList.map((component) => {
            if (component.urlName === 'blackduck') {
                return <Route path={`${uriPrefix}${component.urlName}`} component={BlackDuckConfiguration} />;
            } else if (component.urlName === 'email') {
                return <Route path={`${uriPrefix}${component.urlName}`} component={EmailConfiguration} />;
            } else if (component.urlName === 'hipchat') {
                return <Route path={`${uriPrefix}${component.urlName}`} component={HipChatConfiguration} />;
            } else if (component.urlName === 'slack') {
                return <Route path={`${uriPrefix}${component.urlName}`} component={SlackConfiguration} />;
            }
            return null;
        });

        routeList.unshift(<Route
            exact
            path="/alert/"
            render={() => (
                <Redirect to={`${uriPrefix}${descriptorList[0].urlName}`} />
            )}
        />);
        return routeList;
    }

    render() {
        const channels = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/');
        const providers = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/providers/');
        return (
            <div>
                <Navigation />
                <div className="contentArea">
                    {providers}
                    {channels}
                    <Route path="/alert/jobs/scheduling" component={SchedulingConfiguration} />
                    <Route path="/alert/jobs/distribution" component={DistributionConfiguration} />
                    <Route path="/alert/general/settings" component={SettingsConfiguration} />
                    <Route path="/alert/general/audit" component={Audit} />
                    <Route path="/alert/general/about" component={AboutInfo} />
                </div>
                <div className="modalsArea">
                    <LogoutConfirmation />
                </div>
            </div>);
    }
}

MainPage.propTypes = {
    getDescriptorByType: PropTypes.func.isRequired,
    getDescriptorsByTypeAndContext: PropTypes.func.isRequired,
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired
};
const mapStateToProps = state => ({
    descriptors: state.descriptors
});

const mapDispatchToProps = dispatch => ({
    getDescriptors: () => dispatch(getDescriptors())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
