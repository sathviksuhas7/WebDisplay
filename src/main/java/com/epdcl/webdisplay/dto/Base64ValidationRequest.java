
package com.epdcl.webdisplay.dto;

/**
 * Request body for testing Base64 document validation.
 *
 * Example:
 * {
 *     "image": "iVBORw0KGgo..."
 * }
 */
public record Base64ValidationRequest(
        String image
) {
}
