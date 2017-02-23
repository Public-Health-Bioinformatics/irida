/**
 * Angular service for handling metadata templates
 * @param {object} $resource Angular resource
 * @param {object} $window Angular window
 * @param {object} $httpParamSerializerJQLike Angular param serializer
 * @return {*} Angular resource object for handling metadata templates.
 * @constructor
 */
export function MetadataTemplateService($resource, $window,
                                        $httpParamSerializerJQLike) {
  return $resource($window.PAGE.urls.saveTemplate, {
    id: '@id'
  }, {
    save: {
      method: 'POST',
      transformRequest: function(data) {
        console.log(data);
        return $httpParamSerializerJQLike(data);
      }
    }
  });
}

MetadataTemplateService.$inject = [
  '$resource',
  '$window',
  '$httpParamSerializerJQLike'
];
