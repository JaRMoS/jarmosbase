/**
 * @page jarmos_modelxsd JaRMoS model XSD definition
 * @short The model XSD for any @c model.xml files
 * 
 * This XSD schema can be found in the JaRMoSBase package as @c model.xsd and determines the structure of all
 * JaRMoS-compatible model XML files.
 * @include model.xsd
 */

/**
 * @defgroup jarmosbase JaRMoS Base
 * @short Common base classes for @ref jkermor, @ref jrb, @ref jarmospc and @ref jarmosa
 * 
 * @section jarmos_models JaRMoS model definitions
 * 
 * The models that can be loaded within JaRMoS all have to be described by a suitable XML file. This file has to be
 * named @c model.xml and has to reside in the root folder of the model. The XML file has to match the XSD schema as
 * described in @ref jarmos_modelxsd
 * 
 * Please check the JaRMosModels repository for example @c model.xml instances.
 * 
 * @section jarmosbase_license License conditions
 * 
 * The JaRMoSBase framework as a whole is published under the GNU GPL license stated below.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 * 
 * @{
 * @package jarmos
 * @short JaRMoS main package
 * 
 * @package jarmos.affine
 * @short Contains classes for affine components
 * 
 * @package jarmos.geometry
 * @short Classes for geometry data handling
 * 
 * @package jarmos.io
 * @short Basic I/O classes to load JaRMoS models
 * 
 * @package jarmos.test
 * @short Test classes
 * 
 * @package jarmos.util
 * @short Utility classes
 * 
 * So far contains classes for progress reporting.
 * 
 * @package jarmos.visual
 * @short Common classes for visualization of simulation results
 * 
 * @}
 * 
 * 
 * 
 */
